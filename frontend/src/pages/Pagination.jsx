// src/pages/Pagination.jsx
import React from 'react';
import { ChevronRight } from 'lucide-react';
import styles from '../css/Pagination.module.css'; // 예시: CSS 모듈로 스타일 분리

const Pagination = ({ currentPage, totalPages, onPageChange }) => {
  // 이전 버튼 클릭 핸들러
  const handlePrev = () => {
    if (currentPage > 0) {
      onPageChange(currentPage - 1);
    }
  };

  // 다음 버튼 클릭 핸들러
  const handleNext = () => {
    if (currentPage + 1 < totalPages) {
      onPageChange(currentPage + 1);
    }
  };

  // (선택) 페이지 번호를 직접 눌러 이동하는 UI를 추가할 수도 있음
  const renderPageNumbers = () => {
    const pages = [];
    for (let i = 0; i < totalPages; i++) {
      pages.push(
        <button
          key={i}
          className={`${styles['page-number']} ${
            i === currentPage ? styles['active'] : ''
          }`}
          onClick={() => onPageChange(i)}
        >
          {i + 1}
        </button>
      );
    }
    return pages;
  };

  return (
    <div className={styles['pagination-container']}>
      <button
        onClick={handlePrev}
        disabled={currentPage === 0}
        className={styles['pagination-button']}
      >
        이전
      </button>

      {/* (선택) 아래 부분을 주석 처리하거나, 원하는 방식으로 대체하세요 */}
      <div className={styles['page-numbers']}>
        {renderPageNumbers()}
      </div>

      <button
        onClick={handleNext}
        disabled={currentPage + 1 >= totalPages}
        className={styles['pagination-button']}
      >
        다음
      </button>
    </div>
  );
};

export default Pagination;
