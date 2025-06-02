// src/pages/PasswordInputModal.jsx

import React, { useState, useEffect } from 'react';
import { X, Lock, Eye, EyeOff, Shield } from 'lucide-react';
import styles from '../css/PasswordInputModal.module.css';

/**
 * 비공개 채팅방 입장을 위한 비밀번호 입력 모달
 *
 * @param {Object} props
 * @param {boolean} props.isOpen - 모달 열림 상태
 * @param {() => void} props.onClose - 모달 닫기 함수
 * @param {(password: string) => void} props.onSubmit - 비밀번호 제출 함수
 * @param {string} props.roomName - 입장하려는 채팅방 이름
 * @param {boolean} props.isLoading - 로딩 상태
 * @param {string} props.errorMessage - 에러 메시지
 */
const PasswordInputModal = ({
  isOpen,
  onClose,
  onSubmit,
  roomName,
  isLoading = false,
  errorMessage = ''
}) => {
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);

  // 모달이 열릴 때마다 입력 필드 초기화
  useEffect(() => {
    if (isOpen) {
      setPassword('');
      setShowPassword(false);
    }
  }, [isOpen]);

  const handleSubmit = (e) => {
    e.preventDefault();
    if (password.trim() && !isLoading) {
      onSubmit(password.trim());
    }
  };

  const handleKeyDown = (e) => {
    if (e.key === 'Escape') {
      onClose();
    }
  };

  if (!isOpen) return null;

  return (
    <div className={styles.overlay} onClick={onClose} onKeyDown={handleKeyDown}>
      {/* Background Effects */}
      <div className={styles['background-effects']}>
        <div className={styles['bg-circle-1']} />
        <div className={styles['bg-circle-2']} />
        <div className={styles['bg-circle-3']} />
      </div>

      <div className={styles.container} onClick={(e) => e.stopPropagation()}>
        <div className={styles['gradient-border']} />

        <div className={styles.content}>
          {/* Header */}
          <div className={styles.header}>
            <div className={styles['header-left']}>
              <div className={styles['icon-container']}>
                <Shield className={styles['header-icon']} />
              </div>
              <div className={styles['header-text']}>
                <h2 className={styles.title}>비공개 채팅방</h2>
                <p className={styles.subtitle}>
                  "<span className={styles['room-name']}>{roomName}</span>" 입장을 위해 비밀번호를 입력하세요
                </p>
              </div>
            </div>

            <button onClick={onClose} className={styles['close-button']} disabled={isLoading}>
              <X size={20} />
            </button>
          </div>

          {/* Form */}
          <form onSubmit={handleSubmit} className={styles.form}>
            {/* 에러 메시지 */}
            {errorMessage && (
              <div className={styles['error-message']}>
                <div className={styles['error-icon']}>⚠️</div>
                <span>{errorMessage}</span>
              </div>
            )}

            {/* 비밀번호 입력 */}
            <div className={styles['form-group']}>
              <label className={styles.label}>
                <Lock className={styles['label-icon']} />
                비밀번호
              </label>
              <div className={styles['input-wrapper']}>
                <input
                  type={showPassword ? 'text' : 'password'}
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  placeholder="채팅방 비밀번호를 입력하세요"
                  className={styles.input}
                  disabled={isLoading}
                  autoFocus
                />
                <button
                  type="button"
                  onClick={() => setShowPassword(!showPassword)}
                  className={styles['toggle-password']}
                  disabled={isLoading}
                >
                  {showPassword ? <EyeOff size={16} /> : <Eye size={16} />}
                </button>
                <div className={styles['input-glow']} />
              </div>
            </div>

            {/* 버튼 그룹 */}
            <div className={styles['button-group']}>
              <button
                type="button"
                onClick={onClose}
                className={styles['cancel-button']}
                disabled={isLoading}
              >
                취소
              </button>
              <button
                type="submit"
                disabled={!password.trim() || isLoading}
                className={`${styles['submit-button']} ${isLoading ? styles.loading : ''}`}
              >
                {isLoading ? (
                  <>
                    <div className={styles['loading-spinner']} />
                    <span>확인 중...</span>
                  </>
                ) : (
                  <>
                    <Lock size={16} />
                    <span>입장하기</span>
                  </>
                )}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
};

export default PasswordInputModal;