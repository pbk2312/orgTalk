import React from 'react';
import { Loader2 } from 'lucide-react';
import styles from '../css/LoadingScreen.module.css';

const LoadingScreen = ({ message = '로딩 중...', fullScreen = true }) => {
  const containerClass = fullScreen 
    ? `${styles['loading-screen']} ${styles['fullscreen']}`
    : styles['loading-screen'];

  return (
    <div className={containerClass}>
      <div className={styles['loading-content']}>
        <div className={styles['spinner-container']}>
          <Loader2 className={styles['spinner']} size={48} />
        </div>
        {message && (
          <p className={styles['loading-message']}>{message}</p>
        )}
      </div>
      <div className={styles['background-gradient']}></div>
    </div>
  );
};

export default LoadingScreen;

