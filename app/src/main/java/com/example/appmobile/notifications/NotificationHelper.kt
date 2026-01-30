package com.example.appmobile.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.appmobile.MainActivity
import com.example.appmobile.R

class NotificationHelper(private val context: Context) {

    companion object {
        const val CHANNEL_ID_RAPPELS = "rappels_paiement"
        const val CHANNEL_ID_ALERTES = "alertes_budget"
        const val CHANNEL_ID_OBJECTIFS = "objectifs_investissement"

        const val NOTIFICATION_ID_RAPPEL = 1
        const val NOTIFICATION_ID_BUDGET = 2
        const val NOTIFICATION_ID_OBJECTIF = 3
    }

    init {
        createNotificationChannels()
    }

    // Créer les canaux de notification
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channels = listOf(
                NotificationChannel(
                    CHANNEL_ID_RAPPELS,
                    "Rappels de paiement",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Notifications pour les rappels de paiement"
                },
                NotificationChannel(
                    CHANNEL_ID_ALERTES,
                    "Alertes budget",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Alertes de dépassement de budget"
                },
                NotificationChannel(
                    CHANNEL_ID_OBJECTIFS,
                    "Objectifs d'investissement",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Notifications sur vos objectifs"
                }
            )

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            channels.forEach { notificationManager.createNotificationChannel(it) }
        }
    }

    // Vérifier si les notifications sont autorisées
    fun areNotificationsEnabled(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    // Envoyer une notification de rappel de paiement
    fun sendRappelPaiementNotification(titre: String, montant: Double, joursRestants: Int) {
        if (!areNotificationsEnabled()) return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_RAPPELS)
            .setSmallIcon(R.drawable.logoo)
            .setContentTitle("💳 Rappel de paiement")
            .setContentText("$titre - ${String.format("%.2f", montant)} MAD dans $joursRestants jour(s)")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        with(NotificationManagerCompat.from(context)) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                notify(NOTIFICATION_ID_RAPPEL + titre.hashCode(), notification)
            }
        }
    }

    // Envoyer une alerte de dépassement de budget
    fun sendDepassementBudgetNotification(pourcentage: Double, montantDepasse: Double) {
        if (!areNotificationsEnabled()) return

        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_ALERTES)
            .setSmallIcon(R.drawable.logoo)
            .setContentTitle("⚠️ Dépassement de budget")
            .setContentText("Vous avez dépensé ${pourcentage.toInt()}% de votre budget (${String.format("%.2f", montantDepasse)} MAD)")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Attention ! Vous avez déjà utilisé ${pourcentage.toInt()}% de votre budget mensuel. Pensez à réduire vos dépenses non essentielles.")
            )
            .build()

        with(NotificationManagerCompat.from(context)) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                notify(NOTIFICATION_ID_BUDGET, notification)
            }
        }
    }

    // Envoyer une notification pour un objectif
    fun sendObjectifNotification(titre: String, message: String, type: String) {
        if (!areNotificationsEnabled()) return

        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra("navigate_to", "objectifs")
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val icon = when (type) {
            "OBJECTIF_ATTEINT" -> "🎉"
            "OBJECTIF_RETARD" -> "⚠️"
            "ECHEANCE_PROCHE" -> "⏰"
            else -> "📊"
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_OBJECTIFS)
            .setSmallIcon(R.drawable.logoo)
            .setContentTitle("$icon $titre")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        with(NotificationManagerCompat.from(context)) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                notify(NOTIFICATION_ID_OBJECTIF + titre.hashCode(), notification)
            }
        }
    }
}