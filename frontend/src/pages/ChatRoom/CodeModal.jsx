// src/pages/ChatRoom/CodeModal.jsx
import React, { useEffect, useRef } from 'react';
import styles from '../../css/ChatRoom/CodeModal.module.css';
import { supportedLanguages } from '../../constants/chatConstants';

const CodeModal = ({
  visible,
  selectedLanguage,
  setSelectedLanguage,
  codeInput,
  setCodeInput,
  onSendCode,
  onClose,
  onKeyPress,
  connected,
}) => {
  const textareaRef = useRef(null);

  useEffect(() => {
    if (visible && textareaRef.current) {
      textareaRef.current.focus();
    }
  }, [visible]);

  if (!visible) return null;

  return (
    <div className={styles.codeModalOverlay}>
      <div className={styles.codeModalContent}>
        <div className={styles.codeModalHeader}>
          <h3>코드 블록 추가</h3>
          <button className={styles.closeButton} onClick={onClose}>
            ×
          </button>
        </div>
        <div className={styles.codeModalBody}>
          <div className={styles.languageSelector}>
            <label htmlFor="language-select">언어 선택:</label>
            <select
              id="language-select"
              value={selectedLanguage}
              onChange={(e) => setSelectedLanguage(e.target.value)}
              className={styles.languageSelect}
            >
              {supportedLanguages.map((lang) => (
                <option key={lang.value} value={lang.value}>
                  {lang.label}
                </option>
              ))}
            </select>
          </div>
          <textarea
            ref={textareaRef}
            value={codeInput}
            onChange={(e) => setCodeInput(e.target.value)}
            onKeyDown={onKeyPress}
            placeholder="코드를 입력하세요..."
            className={styles.codeTextarea}
            disabled={!connected}
          />
          <div className={styles.codeModalFooter}>
            <p className={styles.keyboardHint}>Ctrl + Enter로 전송</p>
            <div className={styles.codeModalButtons}>
              <button className={styles.cancelButton} onClick={onClose}>
                취소
              </button>
              <button
                className={styles.sendCodeButton}
                onClick={onSendCode}
                disabled={!codeInput.trim() || !connected}
              >
                전송
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default CodeModal;