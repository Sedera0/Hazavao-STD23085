package hei.examen.prog.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j // Ajout de l'annotation Lombok pour les logs
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
      ResponseEntity<ChatCompletionResponse> responseEntity =
          restTemplate.postForEntity(chatGptApiUrl, requestEntity, ChatCompletionResponse.class);

      if (responseEntity.getStatusCode().is2xxSuccessful() && responseEntity.getBody() != null) {
        ChatCompletionResponse chatResponse = responseEntity.getBody();
        if (chatResponse.getChoices() != null && !chatResponse.getChoices().isEmpty()) {
          return chatResponse.getChoices().getFirst().getMessage().getContent();
        }
      }
      log.warn("ChatGPT API did not return a valid definition for word: {}", word);
      return "Définition introuvable pour le mot : " + word;

    } catch (HttpClientErrorException e) {
      log.error(
          "Erreur de l'API ChatGPT (HTTP {}): {}",
          e.getStatusCode(),
          e.getResponseBodyAsString(),
          e);
      throw new RuntimeException(
          "Erreur de l'API ChatGPT: " + e.getStatusCode() + " - " + e.getResponseBodyAsString(), e);
    } catch (ResourceAccessException e) {
      log.error("Erreur de connexion à l'API ChatGPT: {}", e.getMessage(), e);
      throw new RuntimeException("Problème de connexion à l'API ChatGPT", e);
    } catch (Exception e) {
      log.error("Erreur inattendue lors de l'appel à l'API ChatGPT", e);
      throw new RuntimeException("Erreur inattendue lors de l'appel à l'API ChatGPT", e);
    }
  }
}

@Getter
@Setter
@NoArgsConstructor
class ChatCompletionResponse {
  private String id;
  private String object;
  private long created;
  private String model;
  private List<Choice> choices;
  private Usage usage;
}

@Getter
@Setter
@NoArgsConstructor
class Choice {
  private int index;
  private Message message;

  @JsonProperty("finish_reason")
  private String finishReason;
}

@Getter
@Setter
@NoArgsConstructor
class Message {
  private String role;
  private String content;
}

@Getter
@Setter
@NoArgsConstructor
class Usage {
  @JsonProperty("prompt_tokens")
  private int promptTokens;

  @JsonProperty("completion_tokens")
  private int completionTokens;

  @JsonProperty("total_tokens")
  private int totalTokens;
}
