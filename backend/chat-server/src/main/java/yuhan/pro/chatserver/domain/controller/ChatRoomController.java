package yuhan.pro.chatserver.domain.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import yuhan.pro.chatserver.domain.dto.ChatRoomCreateRequest;
import yuhan.pro.chatserver.domain.dto.ChatRoomCreateResponse;
import yuhan.pro.chatserver.domain.dto.ChatRoomInfoResponse;
import yuhan.pro.chatserver.domain.dto.ChatRoomResponse;
import yuhan.pro.chatserver.domain.dto.JoinChatRoomRequest;
import yuhan.pro.chatserver.domain.service.ChatRoomService;
import yuhan.pro.chatserver.sharedkernel.dto.PageResponse;

@RestController
@RequestMapping("/api/chatroom")
@RequiredArgsConstructor
@Tag(name = "ChatRoom", description = "채팅방 API")
public class ChatRoomController {

  private final ChatRoomService chatRoomService;

  @Operation(summary = "채팅방 생성")
  @ApiResponses({
      @ApiResponse(responseCode = "201", description = "채팅방 생성 성공"),
      @ApiResponse(responseCode = "400", description = "비공개 채팅방, 비밀번호가 비었습니다.")
  })
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ChatRoomCreateResponse createChatRoom(
      @RequestBody @Valid ChatRoomCreateRequest request
  ) {
    return chatRoomService.saveChatRoom(request);
  }


  @Operation(summary = "채팅방 목록 조회")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "채팅방 목록 조회 성공"),
  })
  @GetMapping("/list/{organizationId}")
  @ResponseStatus(HttpStatus.OK)
  public PageResponse<ChatRoomResponse> getChatRooms(
      @PathVariable @Positive Long organizationId,
      @PageableDefault(
          size = 6,
          sort = "lastMessageAt",
          direction = Sort.Direction.DESC
      ) Pageable pageable
  ) {
    return chatRoomService.getChatRooms(organizationId, pageable);
  }

  @Operation(summary = "채팅방 정보 조회")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "채팅방 정보 조회 성공"),
  })
  @GetMapping("/{roomId}")
  @ResponseStatus(HttpStatus.OK)
  public ChatRoomInfoResponse getChatRoomInfo(
      @PathVariable @Positive Long roomId,
      @RequestHeader("Authorization") String authorizationHeader
  ) {
    String jwtToken = authorizationHeader.replace("Bearer ", "");
    return chatRoomService.getChatRoomInfo(roomId, jwtToken);
  }

  @Operation(summary = "채팅방 입장")
  @ApiResponses({
      @ApiResponse(responseCode = "204", description = "채팅방 입장 성공"),
      @ApiResponse(responseCode = "400", description = "비공개방의 비밀번호가 일치하지 않습니다.")
  })
  @PostMapping("/{roomId}/join")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void joinChatRoom(
      @PathVariable @Positive Long roomId,
      @RequestBody @Valid JoinChatRoomRequest request
  ) {
    chatRoomService.joinChatRoom(roomId, request);
  }
}
