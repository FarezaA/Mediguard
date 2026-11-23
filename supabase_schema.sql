-- ====================================================================
-- MediGuard - Skema Database Relasional PostgreSQL (Supabase)
-- Tahap 4: Skema Cloud Database, Realtime Triggers & Row Level Security
-- ====================================================================

-- 1. Enable Extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 2. ENUM Types
DO $$ BEGIN
    CREATE TYPE user_role_enum AS ENUM ('PASIEN', 'PEMANTAU');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

DO $$ BEGIN
    CREATE TYPE room_status_enum AS ENUM ('ACTIVE', 'CLOSED');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

DO $$ BEGIN
    CREATE TYPE membership_status_enum AS ENUM ('PENDING', 'APPROVED', 'REJECTED', 'REVOKED');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

DO $$ BEGIN
    CREATE TYPE dose_status_enum AS ENUM (
        'TERJADWAL',
        'MENUNGGU_LAPORAN',
        'DILAPORKAN_DIMINUM',
        'TERVERIFIKASI_LANGSUNG',
        'TERLEWAT',
        'TERLAMBAT_DILAPORKAN',
        'DIBATALKAN'
    );
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

DO $$ BEGIN
    CREATE TYPE notification_type_enum AS ENUM (
        'REMINDER',
        'JOIN_REQUEST',
        'JOIN_APPROVED',
        'JOIN_REJECTED',
        'DOSE_REPORTED',
        'DOSE_VERIFIED',
        'ACCESS_REVOKED'
    );
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

-- 3. Profiles / Users Table
CREATE TABLE IF NOT EXISTS public.profiles (
    id TEXT PRIMARY KEY DEFAULT gen_random_uuid()::text,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL DEFAULT 'password123',
    active_role user_role_enum NOT NULL DEFAULT 'PASIEN',
    fcm_token TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- 4. Medication Rooms Table
CREATE TABLE IF NOT EXISTS public.rooms (
    id TEXT PRIMARY KEY DEFAULT gen_random_uuid()::text,
    owner_user_id TEXT NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    owner_name VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT DEFAULT '',
    invite_code VARCHAR(32) UNIQUE NOT NULL,
    status room_status_enum DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- 5. Room Members (Hubungan Pemantau & Pasien)
CREATE TABLE IF NOT EXISTS public.room_members (
    id TEXT PRIMARY KEY DEFAULT gen_random_uuid()::text,
    room_id TEXT NOT NULL REFERENCES public.rooms(id) ON DELETE CASCADE,
    user_id TEXT NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    user_name VARCHAR(255) NOT NULL,
    user_email VARCHAR(255) NOT NULL,
    role user_role_enum NOT NULL DEFAULT 'PEMANTAU',
    status membership_status_enum NOT NULL DEFAULT 'PENDING',
    can_view_schedules BOOLEAN DEFAULT TRUE,
    can_view_activities BOOLEAN DEFAULT TRUE,
    can_view_stats BOOLEAN DEFAULT TRUE,
    requested_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    CONSTRAINT unique_room_member UNIQUE(room_id, user_id)
);

-- 6. Medications Table
CREATE TABLE IF NOT EXISTS public.medications (
    id TEXT PRIMARY KEY DEFAULT gen_random_uuid()::text,
    room_id TEXT NOT NULL REFERENCES public.rooms(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    form VARCHAR(50) DEFAULT 'Tablet',
    dosage VARCHAR(100) NOT NULL,
    instructions TEXT NOT NULL,
    notes TEXT DEFAULT '',
    start_date DATE NOT NULL DEFAULT CURRENT_DATE,
    end_date DATE,
    is_archived BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- 7. Schedules Table
CREATE TABLE IF NOT EXISTS public.schedules (
    id TEXT PRIMARY KEY DEFAULT gen_random_uuid()::text,
    medication_id TEXT NOT NULL REFERENCES public.medications(id) ON DELETE CASCADE,
    medication_name VARCHAR(255) NOT NULL,
    frequency VARCHAR(100) NOT NULL DEFAULT '1x sehari',
    times TEXT[] NOT NULL DEFAULT '{"08:00"}',
    start_date DATE NOT NULL DEFAULT CURRENT_DATE,
    end_date DATE,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- 8. Dose Events Table (Instance Jadwal Harian)
CREATE TABLE IF NOT EXISTS public.dose_events (
    id TEXT PRIMARY KEY DEFAULT gen_random_uuid()::text,
    schedule_id TEXT NOT NULL REFERENCES public.schedules(id) ON DELETE CASCADE,
    medication_id TEXT NOT NULL REFERENCES public.medications(id) ON DELETE CASCADE,
    medication_name VARCHAR(255) NOT NULL,
    dosage VARCHAR(100) NOT NULL,
    instructions TEXT NOT NULL,
    scheduled_date DATE NOT NULL DEFAULT CURRENT_DATE,
    scheduled_time VARCHAR(8) NOT NULL, -- Format 'HH:mm'
    status dose_status_enum NOT NULL DEFAULT 'TERJADWAL',
    reported_by_user_id TEXT REFERENCES public.profiles(id),
    reported_by_name VARCHAR(255),
    reported_at TIMESTAMPTZ,
    taken_at VARCHAR(8),
    photo_url TEXT,
    reminder_count INT DEFAULT 0,
    snoozed_until VARCHAR(8),
    notes TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- 9. Dose Verifications Table (Verifikasi Saksi Langsung)
CREATE TABLE IF NOT EXISTS public.dose_verifications (
    id TEXT PRIMARY KEY DEFAULT gen_random_uuid()::text,
    dose_event_id TEXT NOT NULL REFERENCES public.dose_events(id) ON DELETE CASCADE,
    verifier_user_id TEXT NOT NULL REFERENCES public.profiles(id),
    verifier_name VARCHAR(255) NOT NULL,
    verification_type VARCHAR(100) DEFAULT 'Pengamatan Langsung',
    verified_at TIMESTAMPTZ DEFAULT NOW(),
    statement TEXT DEFAULT 'Menyaksikan konsumsi secara langsung'
);

-- 10. In-App Notifications Table
CREATE TABLE IF NOT EXISTS public.notifications (
    id TEXT PRIMARY KEY DEFAULT gen_random_uuid()::text,
    user_id TEXT NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    body TEXT NOT NULL,
    type notification_type_enum NOT NULL DEFAULT 'REMINDER',
    related_entity_id TEXT,
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- 11. Audit Logs Table
CREATE TABLE IF NOT EXISTS public.audit_logs (
    id TEXT PRIMARY KEY DEFAULT gen_random_uuid()::text,
    room_id TEXT NOT NULL REFERENCES public.rooms(id) ON DELETE CASCADE,
    actor_user_id TEXT NOT NULL,
    actor_name VARCHAR(255) NOT NULL,
    action VARCHAR(100) NOT NULL,
    entity_type VARCHAR(100) DEFAULT '',
    entity_id TEXT DEFAULT '',
    details TEXT NOT NULL,
    timestamp TIMESTAMPTZ DEFAULT NOW()
);

-- ====================================================================
-- Indeks Performa
-- ====================================================================
CREATE INDEX IF NOT EXISTS idx_dose_events_date ON public.dose_events(scheduled_date, status);
CREATE INDEX IF NOT EXISTS idx_notifications_user_unread ON public.notifications(user_id, is_read);
CREATE INDEX IF NOT EXISTS idx_room_members_room ON public.room_members(room_id, status);

-- ====================================================================
-- Realtime Replication
-- Mengaktifkan sinkronisasi WebSocket Supabase untuk tabel realtime
-- ====================================================================
ALTER PUBLICATION supabase_realtime ADD TABLE public.dose_events;
ALTER PUBLICATION supabase_realtime ADD TABLE public.notifications;
ALTER PUBLICATION supabase_realtime ADD TABLE public.room_members;

-- ====================================================================
-- Supabase Storage Bucket
-- Bucket aman untuk foto bukti konsumsi obat
-- ====================================================================
INSERT INTO storage.buckets (id, name, public)
VALUES ('medication-proofs', 'medication-proofs', true)
ON CONFLICT (id) DO NOTHING;

-- Kebijakan Akses Storage
DO $$ BEGIN
    CREATE POLICY "Allow authenticated and public read on medication proofs"
    ON storage.objects FOR SELECT
    USING (bucket_id = 'medication-proofs');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

DO $$ BEGIN
    CREATE POLICY "Allow authenticated and anon uploads to medication proofs"
    ON storage.objects FOR INSERT
    WITH CHECK (bucket_id = 'medication-proofs');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;
