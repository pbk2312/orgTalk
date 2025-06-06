// src/pages/Error404Page.jsx

import React, { useEffect, useState } from 'react';
import { Home, ArrowLeft, MapPin, Compass, Github, Code2, Search } from 'lucide-react';
import styles from '../css/Error404Page.module.css';

const Error404Page = () => {
  const [floatingElements, setFloatingElements] = useState([]);

  // 떠다니는 요소들 생성
  useEffect(() => {
    const elements = Array.from({ length: 6 }, (_, i) => ({
      id: i,
      x: Math.random() * 100,
      y: Math.random() * 100,
      delay: Math.random() * 2,
      duration: 3 + Math.random() * 2,
    }));
    setFloatingElements(elements);
  }, []);

  const handleGoBack = () => {
    if (window.history.length > 1) {
      window.history.back();
    } else {
      window.location.href = '/';
    }
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
        <div className={styles.bgGradient1}></div>
        <div className={styles.bgGradient2}></div>
        <div className={styles.bgGradient3}></div>

        {/* 떠다니는 요소들 */}
        {floatingElements.map((element) => (
          <div
            key={element.id}
            className={styles.floatingElement}
            style={{
              left: `${element.x}%`,
              top: `${element.y}%`,
              animationDelay: `${element.delay}s`,
              animationDuration: `${element.duration}s`,
            }}
          >
            404
          </div>
        ))}
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
              <div className={styles.compassContainer}>
                <Compass className={styles.compassIcon} />
                <div className={styles.compassNeedle}></div>
              </div>
              <div className={styles.searchPulse}></div>
            </div>
            <div className={styles.errorCode}>404</div>
            <h1 className={styles.errorTitle}>페이지를 찾을 수 없습니다</h1>
            <p className={styles.errorDescription}>
              요청하신 페이지가 존재하지 않거나 이동되었을 수 있습니다.
              <br />
              아래 방법들을 시도해보세요.
            </p>
          </div>

          {/* 도움말 카드 */}
          <div className={styles.helpCards}>
            <div className={styles.helpCard}>
              <div className={styles.helpCardContent}>
                <div className={`${styles.helpIcon} ${styles.blue}`}>
                  <MapPin />
                </div>
                <div>
                  <h3 className={styles.helpTitle}>경로 확인</h3>
                  <p className={styles.helpDescription}>URL 주소를 다시 확인해보세요</p>
                </div>
              </div>
            </div>
            <div className={styles.helpCard}>
              <div className={styles.helpCardContent}>
                <div className={`${styles.helpIcon} ${styles.green}`}>
                  <Search />
                </div>
                <div>
                  <h3 className={styles.helpTitle}>검색 사용</h3>
                  <p className={styles.helpDescription}>원하는 콘텐츠를 검색해보세요</p>
                </div>
              </div>
            </div>
          </div>

          {/* 액션 버튼들 */}
          <div className={styles.actionButtons}>
            <button
              onClick={handleGoBack}
              className={`${styles.backBtn} ${styles.primaryBtn}`}
            >
              <div className={styles.btnBackground}></div>
              <div className={styles.btnContent}>
                <ArrowLeft />
                <span>이전 페이지</span>
              </div>
            </button>

            <button
              onClick={handleGoHome}
              className={`${styles.homeBtn} ${styles.secondaryBtn}`}
            >
              <div className={styles.btnBackground}></div>
              <div className={styles.btnContent}>
                <Home />
                <span>홈으로 가기</span>
              </div>
            </button>
          </div>

          {/* 지원 섹션 */}
          <div className={styles.supportSection}>
            <p className={styles.supportText}>여전히 찾는 페이지가 없나요?</p>
            <button onClick={handleContactSupport} className={styles.supportBtn}>
              <Github />
              <span>문제 신고하기</span>
            </button>
          </div>
        </div>
      </div>

      {/* 하단 장식 */}
      <div className={styles.bottomDecoration}>
        <div className={styles.decorationLine}></div>
      </div>
    </div>
  );
};

export default Error404Page;
