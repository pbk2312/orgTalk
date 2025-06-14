import React, { useRef, useEffect } from 'react';
import { Send, Code } from 'lucide-react';
import styles from '../../css/ChatRoom/ChatInput.module.css';

const ChatInput = ({
  inputMessage,
  setInputMessage,
  handleSendMessage,
  handleKeyDown,
  isComposing,
  setIsComposing,
  onOpenCodeModal
}) => {
  const inputRef = useRef(null);

  useEffect(() => {
    inputRef.current?.focus();
  }, []);

  return (
    <div className={styles.inputContainer}>
      <textarea
        ref={inputRef}
        className={styles.chatInput}
        value={inputMessage}
        onChange={(e) => setInputMessage(e.target.value)}
        onKeyDown={handleKeyDown}
        onCompositionStart={() => setIsComposing(true)}
        onCompositionEnd={() => setIsComposing(false)}
        placeholder="메시지를 입력하세요. Shift+Enter 줄바꿈"
        rows={1}
      />
      <div className={styles.inputActions}>
        <button onClick={onOpenCodeModal} className={styles.iconButton}>
          <Code size={20} />
        </button>
        <button onClick={handleSendMessage} className={styles.sendButton}>
          <Send size={20} />
        </button>
      </div>
    </div>
  );
};

export default ChatInput;
