package com.example.simpleaichatbot;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.asynchttpclient.DefaultAsyncHttpClient;
import org.asynchttpclient.Response;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class MainActivity extends AppCompatActivity {

    EditText userInput;
    RecyclerView chatRecyclerView;
    ChatAdapter chatAdapter;
    List<ChatMessage> chatMessages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        userInput = findViewById(R.id.user_input);
        chatRecyclerView = findViewById(R.id.chat_recycler_view);

        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(this, chatMessages);
        chatRecyclerView.setAdapter(chatAdapter);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        findViewById(R.id.send_button).setOnClickListener(v -> sendMessageToAI());
    }

    private void sendMessageToAI() {
        String message = userInput.getText().toString();
        addMessageToChat(message, true); // Add user message to chat
        userInput.setText(""); // Clear input field

        new SendMessageTask().execute(message);

    }

    private class SendMessageTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String message = params[0];
            String botReply = ""; // Initialize botReply outside
            try {
                // Perform network request here
                DefaultAsyncHttpClient client = new DefaultAsyncHttpClient();
                CompletableFuture<Response> future = client.preparePost("https://infinite-gpt.p.rapidapi.com/infinite-gpt")
                        .setHeader("x-rapidapi-key", "YOUR_API_KEY") // Replace with your API key
                        .setHeader("x-rapidapi-host", "infinite-gpt.p.rapidapi.com")
                        .setHeader("Content-Type", "application/json")
                        .setBody("{\"query\":\"" + message + "\",\"sysMsg\":\"You are a friendly Chatbot.\"}")
                        .execute()
                        .toCompletableFuture();

                Response response = future.get(); // Wait for response
                String responseString = response.getResponseBody();
                Log.d("TAG", "doInBackground: "+responseString);
                JSONObject jsonResponse = new JSONObject(responseString);
                botReply = jsonResponse.getString("msg");
                if (botReply.endsWith("</s>")) {
                    botReply = botReply.substring(0, botReply.length() - 4);
                }

                client.close();
            } catch (Exception e) {
                e.printStackTrace();
                // Handle error
            }
            return botReply; // Return the bot reply
        }

        @Override
        protected void onPostExecute(String botReply) {
            addMessageToChat(botReply, false);
        }
    }
    private void addMessageToChat(String message, boolean isUserMessage) {
        ChatMessage chatMessage = new ChatMessage(message, isUserMessage);
        chatMessages.add(chatMessage);
        chatAdapter.notifyDataSetChanged();
        chatRecyclerView.scrollToPosition(chatMessages.size() - 1); // Scroll to the last message
    }
}