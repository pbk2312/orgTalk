// src/pages/Pagination.jsx
import React from 'react';
import { ChevronRight } from 'lucide-react';
import styles from '../css/Pagination.module.css'; 

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
