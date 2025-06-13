package hei.examen.prog.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ChatGPTService {

  @Value("${CHAT_GPT_API_KEY}")
  private String chatGptApiKey;

  @Value("${CHAT_GPT_URL}")
  private String chatGptApiUrl;

  private final RestTemplate restTemplate;

  public ChatGPTService(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  public String getMalgacheDefinition(String word) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.setBearerAuth(chatGptApiKey);

    String prompt = "Donne la définition du mot \"" + word + "\" en malgache.";

    Map<String, Object> message = new HashMap<>();
    message.put("role", "user");
    message.put("content", prompt);

    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("model", "gpt-3.5-turbo");
    requestBody.put("messages", Collections.singletonList(message));
    requestBody.put("max_tokens", 150);
    requestBody.put("temperature", 0.7);

    HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

    try {
      String response = restTemplate.postForObject(chatGptApiUrl, requestEntity, String.class);

      assert response != null;
      int contentIndex = response.indexOf("\"content\":\"");
      if (contentIndex != -1) {
        String sub = response.substring(contentIndex + "\"content\":\"".length());
        int endIndex = sub.indexOf("\"");
        if (endIndex != -1) {
          return sub.substring(0, endIndex).replace("\\n", "\n").replace("\\\"", "\"");
        }
      }
      return "Définition introuvable pour le mot : " + word;

    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
