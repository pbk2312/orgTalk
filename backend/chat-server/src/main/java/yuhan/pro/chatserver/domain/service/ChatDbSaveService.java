package yuhan.pro.chatserver.domain.service;


import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import yuhan.pro.chatserver.domain.dto.ChatRequest;

@Service
@RequiredArgsConstructor
public class ChatDbSaveService {

  private final ChatService chatService;
  
  @Async("chatDbExecutor")
  public void saveAsync(ChatRequest message, Long roomId) {
    chatService.saveChat(message, roomId);
  }
}
