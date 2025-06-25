// src/pages/Pagination.jsx
import React from 'react';
import styles from '../css/Pagination.module.css'; 

const Pagination = ({ currentPage, totalPages, onPageChange }) => {
  // 이전 버튼 클릭 핸들러
  const handlePrev = (e) => {
    e.preventDefault();
    e.stopPropagation();
    console.log('Prev button clicked, current page:', currentPage); // 디버깅용 로그
    if (currentPage > 0) {
      onPageChange(currentPage - 1);
    }
  };

  // 다음 버튼 클릭 핸들러
  const handleNext = (e) => {
    e.preventDefault();
    e.stopPropagation();
    console.log('Next button clicked, current page:', currentPage); // 디버깅용 로그
    if (currentPage + 1 < totalPages) {
      onPageChange(currentPage + 1);
    }
  };

  // 페이지 번호 클릭 핸들러
  const handlePageClick = (pageNum, e) => {
    e.preventDefault();
    e.stopPropagation();
    console.log('Page number clicked:', pageNum + 1); // 디버깅용 로그
    if (pageNum !== currentPage) {
      onPageChange(pageNum);
    }
  };

  const renderPageNumbers = () => {
    const pages = [];
    
    // 페이지가 많을 때 일부만 표시하는 로직
    let startPage = Math.max(0, currentPage - 2);
    let endPage = Math.min(totalPages - 1, currentPage + 2);
    
    // 시작 부분 조정
    if (endPage - startPage < 4 && totalPages > 5) {
      if (startPage === 0) {
        endPage = Math.min(totalPages - 1, 4);
      } else if (endPage === totalPages - 1) {
        startPage = Math.max(0, totalPages - 5);
      }
    }

    // 첫 번째 페이지 표시
    if (startPage > 0) {
      pages.push(
        <button
          key={0}
          className={`${styles['page-number']} ${
            0 === currentPage ? styles['active'] : ''
          }`}
          onClick={(e) => handlePageClick(0, e)}
        >
          1
        </button>
      );
      
      if (startPage > 1) {
        pages.push(
          <span key="start-ellipsis" className={styles['ellipsis']}>
            ...
          </span>
        );
      }
    }

    // 중간 페이지들 표시
    for (let i = startPage; i <= endPage; i++) {
      pages.push(
        <button
          key={i}
          className={`${styles['page-number']} ${
            i === currentPage ? styles['active'] : ''
          }`}
          onClick={(e) => handlePageClick(i, e)}
        >
          {i + 1}
        </button>
      );
    }

    // 마지막 페이지 표시
    if (endPage < totalPages - 1) {
      if (endPage < totalPages - 2) {
        pages.push(
          <span key="end-ellipsis" className={styles['ellipsis']}>
            ...
          </span>
        );
      }
      
      pages.push(
        <button
          key={totalPages - 1}
          className={`${styles['page-number']} ${
            totalPages - 1 === currentPage ? styles['active'] : ''
          }`}
          onClick={(e) => handlePageClick(totalPages - 1, e)}
        >
          {totalPages}
        </button>
      );
    }

    return pages;
  };

  // props 유효성 검사
  if (!totalPages || totalPages <= 1) {
    return null;
  }

  return (
    <div className={styles['pagination-container']}>
      <button
        onClick={handlePrev}
        disabled={currentPage === 0}
        className={`${styles['pagination-button']} ${
          currentPage === 0 ? styles['disabled'] : ''
        }`}
        type="button"
      >
        이전
      </button>

      <div className={styles['page-numbers']}>
        {renderPageNumbers()}
      </div>

      <button
        onClick={handleNext}
        disabled={currentPage + 1 >= totalPages}
        className={`${styles['pagination-button']} ${
          currentPage + 1 >= totalPages ? styles['disabled'] : ''
        }`}
        type="button"
      >
        다음
      </button>
    </div>
  );
};

export default Pagination;