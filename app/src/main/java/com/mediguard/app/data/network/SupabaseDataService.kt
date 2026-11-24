package com.mediguard.app.data.network

import com.mediguard.app.data.model.DoseEvent
import com.mediguard.app.data.model.NotificationItem
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SupabaseDoseEventDto(
    val id: String,
    @SerialName("schedule_id") val scheduleId: String,
    @SerialName("medication_id") val medicationId: String,
    @SerialName("medication_name") val medicationName: String,
    val dosage: String,
    val instructions: String,
    @SerialName("scheduled_date") val scheduledDate: String,
    @SerialName("scheduled_time") val scheduledTime: String,
    val status: String,
    @SerialName("reported_by_user_id") val reportedByUserId: String? = null,
    @SerialName("reported_by_name") val reportedByName: String? = null,
    @SerialName("reported_at") val reportedAt: String? = null,
    @SerialName("taken_at") val takenAt: String? = null,
    @SerialName("photo_url") val photoUrl: String? = null,
    @SerialName("reminder_count") val reminderCount: Int = 0,
    @SerialName("snoozed_until") val snoozedUntil: String? = null,
    val notes: String? = null
)

@Serializable
data class SupabaseNotificationDto(
    val id: String,
    @SerialName("user_id") val userId: String,
    val title: String,
    val body: String,
    val type: String,
    @SerialName("related_entity_id") val relatedEntityId: String? = null,
    @SerialName("is_read") val isRead: Boolean = false,
    @SerialName("created_at") val createdAt: String? = null
)

object SupabaseDataService {

    val isAvailable: Boolean
        get() = SupabaseClientProvider.isConfigured

    suspend fun uploadPhoto(bytes: ByteArray, filename: String): Result<String> = withContext(Dispatchers.IO) {
        if (!isAvailable) {
            return@withContext Result.failure(IllegalStateException("Supabase belum dikonfigurasi"))
        }
        try {
            val bucket = SupabaseClientProvider.client.storage.from("medication-proofs")
            bucket.upload(filename, bytes) {
                upsert = true
            }
            val publicUrl = bucket.publicUrl(filename)
            Result.success(publicUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun syncDoseEvents(): Result<List<SupabaseDoseEventDto>> = withContext(Dispatchers.IO) {
        if (!isAvailable) {
            return@withContext Result.failure(IllegalStateException("Supabase belum dikonfigurasi"))
        }
        try {
            val result = SupabaseClientProvider.client.postgrest.from("dose_events")
                .select()
                .decodeList<SupabaseDoseEventDto>()
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendNotification(notification: NotificationItem): Result<Unit> = withContext(Dispatchers.IO) {
        if (!isAvailable) {
            return@withContext Result.failure(IllegalStateException("Supabase belum dikonfigurasi"))
        }
        try {
            val dto = SupabaseNotificationDto(
                id = notification.id,
                userId = notification.userId,
                title = notification.title,
                body = notification.body,
                type = notification.type.name,
                relatedEntityId = notification.relatedEntityId,
                isRead = notification.isRead,
                createdAt = notification.createdAt
            )
            SupabaseClientProvider.client.postgrest.from("notifications").insert(dto)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
