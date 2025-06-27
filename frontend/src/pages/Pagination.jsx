// src/pages/Pagination.jsx
import React from 'react';
import styles from '../css/Pagination.module.css'; 

const Pagination = ({ currentPage, totalPages, onPageChange }) => {

  const handlePrev = (e) => {
    e.preventDefault();
    e.stopPropagation();

    if (currentPage > 0) {
      onPageChange(currentPage - 1);
    }
  };


  const handleNext = (e) => {
    e.preventDefault();
    e.stopPropagation();
    if (currentPage + 1 < totalPages) {
      onPageChange(currentPage + 1);
    }
  };


  const handlePageClick = (pageNum, e) => {
    e.preventDefault();
    e.stopPropagation();

    if (pageNum !== currentPage) {
      onPageChange(pageNum);
    }
  };

  const renderPageNumbers = () => {
    const pages = [];
    

    let startPage = Math.max(0, currentPage - 2);
    let endPage = Math.min(totalPages - 1, currentPage + 2);
    

    if (endPage - startPage < 4 && totalPages > 5) {
      if (startPage === 0) {
        endPage = Math.min(totalPages - 1, 4);
      } else if (endPage === totalPages - 1) {
        startPage = Math.max(0, totalPages - 5);
      }
    }


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