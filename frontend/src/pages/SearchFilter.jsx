import React from 'react';
import { Search, Globe, Lock } from 'lucide-react';
import styles from '../css/SearchFilter.module.css';

const SearchFilter = ({
  searchQuery,
  onSearchChange,
  filterType,
  onFilterChange,
}) => (
  <div className={styles['search-section']}>
    <div className={styles['search-box']}>
      <Search size={20} className={styles['search-icon']} />
      <input
        type="text"
        placeholder="채팅방 검색..."
        value={searchQuery}
        onChange={e => onSearchChange(e.target.value)}
        className={styles['search-input']}
      />
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

export default SearchFilter;
