/**
 * MediGuard Online Server
 * Lightweight backend enabling multi-device real-time sync, camera photo storage,
 * and cross-device notifications between Pasien and Pemantau.
 */

const http = require('http');
const fs = require('fs');
const path = require('path');
const os = require('os');

const PORT = process.env.PORT || 8080;
const DATA_FILE = path.join(__dirname, 'data.json');
const UPLOADS_DIR = path.join(__dirname, 'uploads');

if (!fs.existsSync(UPLOADS_DIR)) {
    fs.mkdirSync(UPLOADS_DIR, { recursive: true });
}

// Helper to get local Wi-Fi / LAN IP addresses
function getLocalIps() {
    const interfaces = os.networkInterfaces();
    const ips = [];
    for (const name of Object.keys(interfaces)) {
        for (const iface of interfaces[name]) {
            if (iface.family === 'IPv4' && !iface.internal) {
                ips.push({ name, ip: iface.address });
            }
        }
    }
    return ips;
}

// Initial Demo Seed Data
function getInitialData() {
    const today = new Date();
    const dateStr = today.toISOString().split('T')[0];

    return {
        users: [
            {
                id: 'user_patient_1',
                name: 'Hendra Wijaya',
                email: 'hendra@mediguard.com',
                password: 'password123',
                roles: ['PASIEN'],
                activeRole: 'PASIEN'
            },
            {
                id: 'user_caregiver_1',
                name: 'Siti Rahmawati',
                email: 'siti@mediguard.com',
                password: 'password123',
                roles: ['PEMANTAU'],
                activeRole: 'PEMANTAU'
            }
        ],
        rooms: [
            {
                id: 'room_1',
                ownerUserId: 'user_patient_1',
                ownerName: 'Hendra Wijaya',
                name: 'Ruang Obat Hendra',
                description: 'Pemantauan pengobatan hipertensi rutin',
                inviteCode: 'MG-8821',
                createdAt: dateStr
            }
        ],
        roomMembers: [
            {
                id: 'member_1',
                roomId: 'room_1',
                userId: 'user_caregiver_1',
                userName: 'Siti Rahmawati',
                userEmail: 'siti@mediguard.com',
                role: 'PEMANTAU',
                status: 'APPROVED',
                requestedAt: dateStr + ' 07:00',
                canViewSchedules: true,
                canViewActivities: true,
                canViewStats: true
            }
        ],
        medications: [
            {
                id: 'med_1',
                roomId: 'room_1',
                name: 'Amlodipine',
                form: 'Tablet',
                dosage: '5 mg',
                instructions: '1x sehari, sesudah makan pagi',
                notes: 'Obat rutin tekanan darah tinggi',
                startDate: dateStr,
                endDate: '',
                isArchived: false
            },
            {
                id: 'med_2',
                roomId: 'room_1',
                name: 'Metformin',
                form: 'Tablet',
                dosage: '500 mg',
                instructions: '2x sehari, bersama makan',
                notes: 'Pengontrol gula darah',
                startDate: dateStr,
                endDate: '',
                isArchived: false
            }
        ],
        schedules: [
            {
                id: 'sched_1',
                medicationId: 'med_1',
                frequency: '1x sehari',
                times: ['08:00'],
                startDate: dateStr,
                endDate: '',
                isActive: true
            },
            {
                id: 'sched_2',
                medicationId: 'med_2',
                frequency: '2x sehari',
                times: ['08:00', '20:00'],
                startDate: dateStr,
                endDate: '',
                isActive: true
            }
        ],
        doseEvents: [
            {
                id: 'dose_1',
                medicationId: 'med_1',
                medicationName: 'Amlodipine',
                dosage: '5 mg',
                instructions: '1x sehari, sesudah makan pagi',
                scheduledTime: '08:00',
                status: 'MENUNGGU_LAPORAN',
                takenAt: null,
                reportedAt: null,
                verification: null,
                notes: null,
                snoozedUntil: null,
                reminderCount: 1,
                photoUri: null
            },
            {
                id: 'dose_2',
                medicationId: 'med_2',
                medicationName: 'Metformin',
                dosage: '500 mg',
                instructions: '2x sehari, bersama makan',
                scheduledTime: '08:00',
                status: 'DILAPORKAN_DIMINUM',
                takenAt: '08:15',
                reportedAt: dateStr + ' 08:15',
                verification: null,
                notes: 'Sudah diminum bersama sarapan bubur',
                snoozedUntil: null,
                reminderCount: 0,
                photoUri: '/uploads/sample_metformin.jpg'
            },
            {
                id: 'dose_3',
                medicationId: 'med_2',
                medicationName: 'Metformin',
                dosage: '500 mg',
                instructions: '2x sehari, bersama makan',
                scheduledTime: '20:00',
                status: 'TERJADWAL',
                takenAt: null,
                reportedAt: null,
                verification: null,
                notes: null,
                snoozedUntil: null,
                reminderCount: 0,
                photoUri: null
            }
        ],
        notifications: [
            {
                id: 'notif_1',
                userId: 'user_caregiver_1',
                title: 'Laporan Baru dari Pasien',
                body: 'Hendra telah melaporkan minum Metformin (08:00). Silakan verifikasi bila menyaksikan langsung.',
                type: 'DOSE_REPORTED',
                relatedId: 'dose_2',
                isRead: false,
                createdAt: dateStr + ' 08:16'
            },
            {
                id: 'notif_2',
                userId: 'user_patient_1',
                title: 'Jadwal Obat Pagi',
                body: 'Saatnya minum Amlodipine 5 mg (Jadwal: 08:00). Jangan lupa ambil foto bukti minum obat!',
                type: 'REMINDER',
                relatedId: 'dose_1',
                isRead: false,
                createdAt: dateStr + ' 08:00'
            }
        ],
        auditLogs: [
            {
                id: 'log_1',
                actorId: 'user_patient_1',
                actorName: 'Hendra Wijaya',
                action: 'LAPORAN_MANDIRI',
                details: 'Melaporkan minum obat Metformin (08:00) pada 08:15.',
                timestamp: dateStr + ' 08:15'
            },
            {
                id: 'log_2',
                actorId: 'system',
                actorName: 'Sistem MediGuard',
                action: 'PENGINGAT_OTOMATIS',
                details: 'Notifikasi pengingat otomatis dikirim ke Hendra untuk Amlodipine (08:00).',
                timestamp: dateStr + ' 08:00'
            }
        ],
        version: 1
    };
}

// Load or initialize state
let state;
try {
    if (fs.existsSync(DATA_FILE)) {
        state = JSON.parse(fs.readFileSync(DATA_FILE, 'utf8'));
    } else {
        state = getInitialData();
        fs.writeFileSync(DATA_FILE, JSON.stringify(state, null, 2), 'utf8');
    }
} catch (e) {
    console.error('Error loading data.json, re-initializing:', e);
    state = getInitialData();
}

function saveState() {
    state.version = (state.version || 0) + 1;
    fs.writeFileSync(DATA_FILE, JSON.stringify(state, null, 2), 'utf8');
}

// HTTP Server
const server = http.createServer((req, res) => {
    // CORS headers
    res.setHeader('Access-Control-Allow-Origin', '*');
    res.setHeader('Access-Control-Allow-Methods', 'GET, POST, OPTIONS, PUT, DELETE');
    res.setHeader('Access-Control-Allow-Headers', 'Content-Type, Authorization, x-user-id');

    if (req.method === 'OPTIONS') {
        res.writeHead(204);
        res.end();
        return;
    }

    const url = new URL(req.url, `http://${req.headers.host || 'localhost'}`);
    const pathname = url.pathname;

    // ── Static Files in /uploads ──
    if (pathname.startsWith('/uploads/')) {
        const filename = path.basename(pathname);
        const filePath = path.join(UPLOADS_DIR, filename);

        if (fs.existsSync(filePath)) {
            const ext = path.extname(filename).toLowerCase();
            const mime = ext === '.png' ? 'image/png' : 'image/jpeg';
            res.writeHead(200, { 'Content-Type': mime, 'Cache-Control': 'public, max-age=3600' });
            fs.createReadStream(filePath).pipe(res);
        } else {
            res.writeHead(404, { 'Content-Type': 'application/json' });
            res.end(JSON.stringify({ error: 'File not found' }));
        }
        return;
    }

    // ── GET /api/status ──
    if (req.method === 'GET' && pathname === '/api/status') {
        const localIps = getLocalIps();
        res.writeHead(200, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({
            status: 'online',
            service: 'MediGuard Realtime Server',
            port: PORT,
            localIps: localIps,
            recommendedUrl: localIps.length > 0 ? `http://${localIps[0].ip}:${PORT}` : `http://localhost:${PORT}`,
            stats: {
                users: state.users.length,
                rooms: state.rooms.length,
                doses: state.doseEvents.length,
                version: state.version
            }
        }));
        return;
    }

    // ── GET /api/data (Sync all data) ──
    if (req.method === 'GET' && pathname === '/api/data') {
        res.writeHead(200, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify(state));
        return;
    }

    // Helper to read JSON request body
    function readJsonBody(callback) {
        let body = '';
        req.on('data', chunk => {
            body += chunk;
            if (body.length > 20 * 1024 * 1024) { // 20MB limit
                res.writeHead(413, { 'Content-Type': 'application/json' });
                res.end(JSON.stringify({ error: 'Payload too large' }));
                req.destroy();
            }
        });
        req.on('end', () => {
            try {
                const parsed = body ? JSON.parse(body) : {};
                callback(null, parsed);
            } catch (err) {
                callback(err, null);
            }
        });
    }

    // ── POST /api/upload (Photo upload from device camera) ──
    if (req.method === 'POST' && pathname === '/api/upload') {
        readJsonBody((err, data) => {
            if (err || !data.imageBase64) {
                res.writeHead(400, { 'Content-Type': 'application/json' });
                res.end(JSON.stringify({ error: 'Invalid payload: imageBase64 is required' }));
                return;
            }

            try {
                const buffer = Buffer.from(data.imageBase64, 'base64');
                const filename = data.filename || `bukti_kamera_${Date.now()}_${Math.floor(Math.random() * 1000)}.jpg`;
                const safeName = path.basename(filename);
                const filePath = path.join(UPLOADS_DIR, safeName);

                fs.writeFileSync(filePath, buffer);

                // Build accessible URL
                const host = req.headers.host || `localhost:${PORT}`;
                const fileUrl = `http://${host}/uploads/${safeName}`;

                res.writeHead(200, { 'Content-Type': 'application/json' });
                res.end(JSON.stringify({
                    success: true,
                    filename: safeName,
                    url: fileUrl,
                    relativePath: `/uploads/${safeName}`
                }));
            } catch (uploadErr) {
                console.error('Error saving uploaded photo:', uploadErr);
                res.writeHead(500, { 'Content-Type': 'application/json' });
                res.end(JSON.stringify({ error: 'Failed to save photo' }));
            }
        });
        return;
    }

    // ── POST /api/action (Unified action dispatcher) ──
    if (req.method === 'POST' && pathname === '/api/action') {
        readJsonBody((err, data) => {
            if (err) {
                res.writeHead(400, { 'Content-Type': 'application/json' });
                res.end(JSON.stringify({ error: 'Invalid JSON' }));
                return;
            }

            const { action, payload, userId } = data;
            const now = new Date();
            const timeStr = `${String(now.getHours()).padStart(2, '0')}:${String(now.getMinutes()).padStart(2, '0')}`;
            const dateStr = now.toISOString().split('T')[0] + ' ' + timeStr;

            const currentUser = state.users.find(u => u.id === userId) || state.users[0];

            switch (action) {
                case 'reportDoseTaken': {
                    const { doseEventId, actualTakenTime, photoUri } = payload;
                    const dose = state.doseEvents.find(d => d.id === doseEventId);
                    if (dose) {
                        dose.status = 'DILAPORKAN_DIMINUM';
                        dose.takenAt = actualTakenTime || timeStr;
                        dose.reportedAt = dateStr;
                        if (photoUri) dose.photoUri = photoUri;

                        state.auditLogs.unshift({
                            id: `log_${Date.now()}`,
                            actorId: currentUser.id,
                            actorName: currentUser.name,
                            action: 'LAPORAN_MANDIRI',
                            details: `Melaporkan minum obat ${dose.medicationName} (${dose.dosage}) pada ${actualTakenTime || timeStr}.`,
                            timestamp: dateStr
                        });

                        // Notify caregivers in the room
                        const room = state.rooms[0];
                        const caregivers = state.roomMembers.filter(m => m.roomId === (room?.id || 'room_1') && m.role === 'PEMANTAU' && m.status === 'APPROVED');
                        caregivers.forEach(c => {
                            state.notifications.unshift({
                                id: `notif_${Date.now()}_${Math.random()}`,
                                userId: c.userId,
                                title: 'Laporan Minum Obat Masuk',
                                body: `${currentUser.name} telah melaporkan minum ${dose.medicationName} (${dose.dosage}) pada ${actualTakenTime || timeStr}. Silakan verifikasi jika menyaksikan langsung.`,
                                type: 'DOSE_REPORTED',
                                relatedId: dose.id,
                                isRead: false,
                                createdAt: dateStr
                            });
                        });

                        saveState();
                    }
                    break;
                }

                case 'reportDoseMissed': {
                    const { doseEventId, reason } = payload;
                    const dose = state.doseEvents.find(d => d.id === doseEventId);
                    if (dose) {
                        dose.status = 'TERLEWAT';
                        dose.reportedAt = dateStr;
                        dose.notes = reason ? `Alasan: ${reason}` : 'Tanpa alasan';

                        state.auditLogs.unshift({
                            id: `log_${Date.now()}`,
                            actorId: currentUser.id,
                            actorName: currentUser.name,
                            action: 'DOSIS_TERLEWAT',
                            details: `Mencatat dosis ${dose.medicationName} terlewat. ${dose.notes}`,
                            timestamp: dateStr
                        });

                        // Notify caregivers
                        const caregivers = state.roomMembers.filter(m => m.role === 'PEMANTAU' && m.status === 'APPROVED');
                        caregivers.forEach(c => {
                            state.notifications.unshift({
                                id: `notif_${Date.now()}_${Math.random()}`,
                                userId: c.userId,
                                title: 'Dosis Terlewat',
                                body: `${currentUser.name} mencatat jadwal ${dose.medicationName} terlewat (${dose.notes}).`,
                                type: 'DOSE_MISSED',
                                relatedId: dose.id,
                                isRead: false,
                                createdAt: dateStr
                            });
                        });

                        saveState();
                    }
                    break;
                }

                case 'snoozeDose': {
                    const { doseEventId, minutes } = payload;
                    const dose = state.doseEvents.find(d => d.id === doseEventId);
                    if (dose) {
                        const snoozeDate = new Date(Date.now() + (minutes || 15) * 60000);
                        const snoozeStr = `${String(snoozeDate.getHours()).padStart(2, '0')}:${String(snoozeDate.getMinutes()).padStart(2, '0')}`;
                        dose.snoozedUntil = snoozeStr;

                        state.auditLogs.unshift({
                            id: `log_${Date.now()}`,
                            actorId: currentUser.id,
                            actorName: currentUser.name,
                            action: 'TUNDA_PENGINGAT',
                            details: `Menunda pengingat ${dose.medicationName} selama ${minutes} menit (sampai ${snoozeStr}).`,
                            timestamp: dateStr
                        });
                        saveState();
                    }
                    break;
                }

                case 'verifyDose': {
                    const { doseEventId, directObservationConfirmed, notes } = payload;
                    const dose = state.doseEvents.find(d => d.id === doseEventId);
                    if (dose && directObservationConfirmed) {
                        dose.status = 'TERVERIFIKASI_LANGSUNG';
                        dose.verification = {
                            verifiedByUserId: currentUser.id,
                            verifierName: currentUser.name,
                            verifiedAt: dateStr,
                            statement: 'Menyaksikan langsung saat obat dikonsumsi',
                            isDirectObservation: true,
                            notes: notes || null
                        };

                        state.auditLogs.unshift({
                            id: `log_${Date.now()}`,
                            actorId: currentUser.id,
                            actorName: currentUser.name,
                            action: 'VERIFIKASI_LANGSUNG',
                            details: `Memverifikasi secara nyata konsumsi ${dose.medicationName} oleh pasien.`,
                            timestamp: dateStr
                        });

                        // Notify patient
                        const patientId = state.rooms[0]?.ownerUserId || 'user_patient_1';
                        state.notifications.unshift({
                            id: `notif_${Date.now()}_${Math.random()}`,
                            userId: patientId,
                            title: 'Dosis Terverifikasi Nyata ✓',
                            body: `${currentUser.name} telah memverifikasi konsumsi obat ${dose.medicationName} Anda melalui pengamatan langsung.`,
                            type: 'DOSE_VERIFIED',
                            relatedId: dose.id,
                            isRead: false,
                            createdAt: dateStr
                        });

                        saveState();
                    }
                    break;
                }

                case 'sendReminder': {
                    const { doseEventId } = payload;
                    const dose = state.doseEvents.find(d => d.id === doseEventId);
                    if (dose) {
                        dose.reminderCount = (dose.reminderCount || 0) + 1;

                        state.auditLogs.unshift({
                            id: `log_${Date.now()}`,
                            actorId: currentUser.id,
                            actorName: currentUser.name,
                            action: 'KIRIM_PENGINGAT',
                            details: `Mengirimkan pengingat ke-${dose.reminderCount} untuk ${dose.medicationName}.`,
                            timestamp: dateStr
                        });

                        const patientId = state.rooms[0]?.ownerUserId || 'user_patient_1';
                        state.notifications.unshift({
                            id: `notif_${Date.now()}_${Math.random()}`,
                            userId: patientId,
                            title: 'Pengingat dari Pemantau 🔔',
                            body: `${currentUser.name} mengingatkan Anda untuk meminum ${dose.medicationName} (${dose.dosage}).`,
                            type: 'REMINDER',
                            relatedId: dose.id,
                            isRead: false,
                            createdAt: dateStr
                        });

                        saveState();
                    }
                    break;
                }

                case 'requestJoinRoom': {
                    const { inviteCode } = payload;
                    const cleanCode = (inviteCode || '').toUpperCase().trim();
                    const cleanNoPrefix = cleanCode.replace(/^MG-?/, '');
                    const room = state.rooms.find(r => 
                        r.inviteCode.toUpperCase() === cleanCode || 
                        r.id.toUpperCase() === cleanCode ||
                        r.inviteCode.replace(/^MG-?/, '').toUpperCase() === cleanNoPrefix
                    );
                    if (room) {
                        const existing = state.roomMembers.find(m => m.roomId === room.id && m.userId === currentUser.id);
                        if (!existing) {
                            const newMember = {
                                id: `member_${Date.now()}`,
                                roomId: room.id,
                                userId: currentUser.id,
                                userName: currentUser.name,
                                userEmail: currentUser.email,
                                role: 'PEMANTAU',
                                status: 'PENDING',
                                requestedAt: dateStr,
                                canViewSchedules: true,
                                canViewActivities: true,
                                canViewStats: false
                            };
                            state.roomMembers.push(newMember);

                            state.notifications.unshift({
                                id: `notif_${Date.now()}`,
                                userId: room.ownerUserId,
                                title: 'Permintaan Bergabung Baru',
                                body: `${currentUser.name} (${currentUser.email}) meminta izin untuk memantau ${room.name}.`,
                                type: 'ROOM_REQUEST',
                                relatedId: newMember.id,
                                isRead: false,
                                createdAt: dateStr
                            });

                            saveState();
                        }
                    }
                    break;
                }

                case 'approveMember': {
                    const { memberId } = payload;
                    const member = state.roomMembers.find(m => m.id === memberId);
                    if (member) {
                        member.status = 'APPROVED';

                        state.notifications.unshift({
                            id: `notif_${Date.now()}`,
                            userId: member.userId,
                            title: 'Permintaan Diterima ✓',
                            body: `Pasien telah menyetujui akses Anda untuk memantau ruang obat.`,
                            type: 'ROOM_APPROVED',
                            relatedId: member.id,
                            isRead: false,
                            createdAt: dateStr
                        });

                        saveState();
                    }
                    break;
                }

                case 'rejectMember': {
                    const { memberId } = payload;
                    const member = state.roomMembers.find(m => m.id === memberId);
                    if (member) {
                        member.status = 'REJECTED';
                        saveState();
                    }
                    break;
                }

                case 'rotateInviteCode': {
                    const { roomId, newInviteCode } = payload;
                    const room = state.rooms.find(r => r.id === roomId);
                    if (room) {
                        const randomNum = Math.floor(1000 + Math.random() * 9000);
                        room.inviteCode = newInviteCode || `MG-${randomNum}`;
                        saveState();
                    }
                    break;
                }

                case 'createRoom': {
                    const { id, name, description, inviteCode } = payload;
                    const randomNum = Math.floor(1000 + Math.random() * 9000);
                    const newRoom = {
                        id: id || `room_${Date.now()}`,
                        ownerUserId: currentUser.id,
                        ownerName: currentUser.name,
                        name: name,
                        description: description || '',
                        inviteCode: inviteCode || `MG-${randomNum}`,
                        createdAt: dateStr
                    };
                    state.rooms.push(newRoom);

                    // Add owner member if not present
                    const existingOwner = state.roomMembers.find(m => m.roomId === newRoom.id && m.userId === currentUser.id);
                    if (!existingOwner) {
                        state.roomMembers.push({
                            id: `member_${Date.now()}`,
                            roomId: newRoom.id,
                            userId: currentUser.id,
                            userName: currentUser.name,
                            userEmail: currentUser.email,
                            role: 'PASIEN',
                            status: 'APPROVED',
                            canViewSchedules: true,
                            canViewActivities: true,
                            canViewStats: true,
                            requestedAt: dateStr,
                            updatedAt: dateStr
                        });
                    }

                    saveState();
                    break;
                }

                case 'updateRoom': {
                    const { roomId, name, description } = payload;
                    const room = state.rooms.find(r => r.id === roomId);
                    if (room) {
                        if (name) room.name = name;
                        if (description !== undefined) room.description = description;
                        saveState();
                    }
                    break;
                }

                case 'deleteRoom': {
                    const { roomId } = payload;
                    state.rooms = state.rooms.filter(r => r.id !== roomId);
                    state.roomMembers = state.roomMembers.filter(m => m.roomId !== roomId);
                    saveState();
                    break;
                }

                case 'addMedication': {
                    const { roomId, name, form, dosage, instructions, notes } = payload;
                    const newMed = {
                        id: `med_${Date.now()}`,
                        roomId: roomId || state.rooms[0]?.id || 'room_1',
                        name,
                        form,
                        dosage,
                        instructions,
                        notes: notes || '',
                        startDate: dateStr,
                        endDate: '',
                        isArchived: false
                    };
                    state.medications.push(newMed);

                    // Add an initial dose event
                    state.doseEvents.push({
                        id: `dose_${Date.now()}`,
                        medicationId: newMed.id,
                        medicationName: newMed.name,
                        dosage: newMed.dosage,
                        instructions: newMed.instructions,
                        scheduledTime: '12:00',
                        status: 'MENUNGGU_LAPORAN',
                        takenAt: null,
                        reportedAt: null,
                        verification: null,
                        notes: null,
                        snoozedUntil: null,
                        reminderCount: 0,
                        photoUri: null
                    });

                    saveState();
                    break;
                }

                case 'archiveMedication': {
                    const { medicationId } = payload;
                    const med = state.medications.find(m => m.id === medicationId);
                    if (med) {
                        med.isArchived = true;
                        saveState();
                    }
                    break;
                }

                case 'addSchedule': {
                    const { medicationId, frequency, times } = payload;
                    state.schedules.push({
                        id: `sched_${Date.now()}`,
                        medicationId,
                        frequency,
                        times: times || ['08:00'],
                        startDate: dateStr,
                        endDate: '',
                        isActive: true
                    });
                    saveState();
                    break;
                }

                case 'updatePermissions': {
                    const { memberId, canViewSchedules, canViewActivities, canViewStats } = payload;
                    const member = state.roomMembers.find(m => m.id === memberId);
                    if (member) {
                        member.canViewSchedules = canViewSchedules;
                        member.canViewActivities = canActivities;
                        member.canViewStats = canStats;
                        saveState();
                    }
                    break;
                }

                case 'markNotificationRead': {
                    const { notificationId } = payload;
                    const notif = state.notifications.find(n => n.id === notificationId);
                    if (notif) {
                        notif.isRead = true;
                        saveState();
                    }
                    break;
                }

                case 'markAllNotificationsRead': {
                    state.notifications.forEach(n => {
                        if (n.userId === currentUser.id) n.isRead = true;
                    });
                    saveState();
                    break;
                }

                case 'seedDemo': {
                    state = getInitialData();
                    saveState();
                    break;
                }
            }

            res.writeHead(200, { 'Content-Type': 'application/json' });
            res.end(JSON.stringify({ success: true, version: state.version, state }));
        });
        return;
    }

    // 404 for unknown endpoints
    res.writeHead(404, { 'Content-Type': 'application/json' });
    res.end(JSON.stringify({ error: 'Endpoint not found' }));
});

server.listen(PORT, '0.0.0.0', () => {
    const localIps = getLocalIps();
    console.log(`=======================================================`);
    console.log(` MediGuard Online Backend is RUNNING on port ${PORT}`);
    console.log(` Local:   http://localhost:${PORT}`);
    localIps.forEach(i => {
        console.log(` Wi-Fi:   http://${i.ip}:${PORT}  (${i.name})`);
    });
    console.log(` Emulator: http://10.0.2.2:${PORT}`);
    console.log(`=======================================================`);
});
