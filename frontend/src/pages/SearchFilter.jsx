// src/components/SearchFilter.jsx - 수정된 버전
import React from 'react';
import { Search, Globe, Lock, X, RotateCcw, Filter } from 'lucide-react';
import styles from '../css/SearchFilter.module.css';

const SearchFilter = ({
  searchQuery,
  onSearchChange,
  onSearchSubmit,
  onSearchClear,
  filterType,
  onFilterChange,
  totalElements = 0,
  activeSearchQuery = '',
  currentPage = 0,
  totalPages = 0,
}) => {
  const handleKeyPress = (e) => {
    if (e.key === 'Enter') {
      onSearchSubmit();
    }
  };

  const handleClear = () => {
    onSearchClear();
  };

  const handleResetAll = () => {
    onSearchClear();
    onFilterChange('all');
  };

  const hasActiveSearch = activeSearchQuery.trim();
  const hasActiveFilter = filterType !== 'all';
  
  // 결과 텍스트 생성 - 백엔드 필터링 결과만 사용
  const getResultText = () => {
    // 검색어와 필터가 모두 있는 경우
    if (hasActiveSearch && hasActiveFilter) {
      const filterName = filterType === 'public' ? '공개' : '비공개';
      return `"${activeSearchQuery}" 검색 결과 (${filterName} 채팅방)`;
    }
    // 검색어만 있는 경우
    else if (hasActiveSearch) {
      return `"${activeSearchQuery}" 검색 결과`;
    }
    // 필터만 있는 경우
    else if (hasActiveFilter) {
      const filterName = filterType === 'public' ? '공개' : '비공개';
      return `${filterName} 채팅방`;
    }
    return null;
  };

  const resultText = getResultText();
  
  // 페이징 정보 표시 - totalElements는 이미 백엔드에서 필터링된 결과
  const getDetailedResultText = () => {
    if (!resultText) return null;
    
    if (totalPages > 1) {
      const startItem = currentPage * 9 + 1;
      const endItem = Math.min((currentPage + 1) * 9, totalElements);
      return `${resultText} · ${startItem}-${endItem} / ${totalElements}개`;
    } else {
      return `${resultText} · ${totalElements}개`;
    }
  };

  return (
    <div className={styles['search-section']}>
      {/* 검색/필터 상태가 있을 때 표시 */}
      {(hasActiveSearch || hasActiveFilter) && (
        <div className={styles['search-status']}>
          <div className={styles['status-info']}>
            <Filter size={16} className={styles['status-icon']} />
            <span className={styles['status-text']}>
              {getDetailedResultText()}
            </span>
          </div>
          <button
            onClick={handleResetAll}
            className={styles['reset-button']}
            title="전체 목록 보기"
          >
            <RotateCcw size={16} />
            <span>전체 목록</span>
          </button>
        </div>
      )}

      <div className={styles['search-box']}>
        <Search size={20} className={styles['search-icon']} />
        <input
          type="text"
          placeholder="채팅방 검색 후 엔터를 누르세요..."
          value={searchQuery}
          onChange={e => onSearchChange(e.target.value)}
          onKeyPress={handleKeyPress}
          className={styles['search-input']}
        />
        {searchQuery && (
          <button
            onClick={handleClear}
            className={styles['clear-button']}
            type="button"
            title="검색어 지우기"
          >
            <X size={16} />
          </button>
        )}
      </div>
      
      <div className={styles['filter-buttons']}>
        {['all','public','private'].map(type => (
          <button
            key={type}
            onClick={() => onFilterChange(type)}
            className={`${styles['filter-button']} ${filterType===type?styles.active:''}`}
          >
            {type==='public' ? <><Globe size={16}/> 공개</>
             : type==='private'? <><Lock size={16}/> 비공개</>
             : '전체'}
          </button>
        ))}
      </div>
    </div>
  );
};

export default SearchFilter;