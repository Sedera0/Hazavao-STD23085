package hei.examen.prog.endpoint;

import hei.examen.prog.service.ChatGPTService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HazavaoController {

  private final ChatGPTService chatGPTService;

  public HazavaoController(ChatGPTService chatGPTService) {
    this.chatGPTService = chatGPTService;
  }

  @GetMapping("/hazavao")
  public String getDefinition(@RequestParam String teny) {
    if (teny == null || teny.trim().isEmpty()) {
      return "Veuillez fournir un mot avec le paramètre 'teny'. Exemple: /hazavao?teny=fihavanana";
    }
    return chatGPTService.getMalgacheDefinition(teny);
  }
}
