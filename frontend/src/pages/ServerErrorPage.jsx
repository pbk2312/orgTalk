// src/pages/ServerError.jsx

import React, { useState } from 'react';
import { AlertTriangle, RefreshCw, Home, Github, Code2 } from 'lucide-react';
import styles from '../css/Error500Page.module.css';

const Error500Page = () => {
  const [isRetrying, setIsRetrying] = useState(false);
  const [retryCount, setRetryCount] = useState(0);

  const handleRetry = () => {
    setIsRetrying(true);
    setRetryCount(prev => prev + 1);
    
    setTimeout(() => {
      setIsRetrying(false);
  
      window.location.reload();
    }, 2000);
  };

  const handleGoHome = () => {
    window.location.href = '/';
  };

  const handleContactSupport = () => {
    window.open('https://github.com/orgtalk/support/issues', '_blank');
  };

  return (
    <div className={styles.errorPage}>
      {/* 배경 효과 */}
      <div className={styles.backgroundEffects}>
        <div className={styles.bgCircle1}></div>
        <div className={styles.bgCircle2}></div>
        <div className={styles.bgCircle3}></div>
      </div>

      {/* 헤더 */}
      <div className={styles.errorHeader}>
        <div className={styles.errorLogo}>
          <Code2 />
        </div>
        <span className={styles.errorBrand}>
          Org<span className={styles.brandHighlight}>Talk</span>
        </span>
      </div>

      {/* 메인 콘텐츠 */}
      <div className={styles.mainContent}>
        <div className={styles.errorContainer}>
          {/* 에러 아이콘 및 상태 */}
          <div className={styles.errorIconSection}>
            <div className={styles.errorIconWrapper}>
              <AlertTriangle className={styles.errorIcon} />
              <div className={styles.errorPulse}></div>
            </div>
            <div className={styles.errorCode}>500</div>
            <h1 className={styles.errorTitle}>서버 오류가 발생했습니다</h1>
            <p className={styles.errorDescription}>
              죄송합니다. 서버에서 예상치 못한 오류가 발생했습니다.<br />
              잠시 후 다시 시도해 주세요.
            </p>
          </div>

          {/* 상태 정보 카드 */}
          <div className={styles.statusCards}>
            <div className={styles.statusCard}>
              <div className={styles.statusCardContent}>
                <div className={`${styles.statusIcon} ${styles.red}`}>
                  <AlertTriangle />
                </div>
                <div>
                  <h3 className={styles.statusTitle}>서버 상태</h3>
                  <p className={styles.statusDescription}>일시적 장애 감지됨</p>
                </div>
              </div>
            </div>
            <div className={styles.statusCard}>
              <div className={styles.statusCardContent}>
                <div className={`${styles.statusIcon} ${styles.orange}`}>
                  <RefreshCw />
                </div>
                <div>
                  <h3 className={styles.statusTitle}>복구 작업</h3>
                  <p className={styles.statusDescription}>자동 복구 진행 중</p>
                </div>
              </div>
            </div>
          </div>

          {/* 액션 버튼들 */}
          <div className={styles.actionButtons}>
            <button
              onClick={handleRetry}
              disabled={isRetrying}
              className={`${styles.retryBtn} ${styles.primaryBtn}`}
            >
              <div className={styles.btnBackground}></div>
              {isRetrying ? (
                <div className={styles.loadingContent}>
                  <div className={styles.loadingSpinner}></div>
                  <span>재시도 중...</span>
                </div>
              ) : (
                <div className={styles.btnContent}>
                  <RefreshCw />
                  <span>다시 시도</span>
                </div>
              )}
            </button>

            <button
              onClick={handleGoHome}
              className={`${styles.homeBtn} ${styles.secondaryBtn}`}
            >
              <div className={styles.btnBackground}></div>
              <div className={styles.btnContent}>
                <Home />
                <span>홈으로 돌아가기</span>
              </div>
            </button>
          </div>

          {/* 지원 섹션 */}
          <div className={styles.supportSection}>
            <p className={styles.supportText}>
              문제가 지속된다면 개발팀에 문의해 주세요
            </p>
            <button
              onClick={handleContactSupport}
              className={styles.supportBtn}
            >
              <Github />
              <span>GitHub에서 문의하기</span>
            </button>
          </div>

          {/* 재시도 횟수 표시 */}
          {retryCount > 0 && (
            <div className={styles.retryInfo}>
              <div className={styles.retryBadge}>
                <div className={styles.retryDot}></div>
                <span className={styles.retryText}>재시도 횟수: {retryCount}</span>
              </div>
            </div>
          )}
        </div>
      </div>

      {/* 하단 장식 */}
      <div className={styles.bottomDecoration}></div>
    </div>
  );
};

export default Error500Page;