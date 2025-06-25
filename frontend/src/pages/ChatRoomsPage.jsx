// src/components/ChatRoomsPage.jsx
import React, { useState, useEffect, useCallback } from 'react';
import {
  MessageCircle,
  Users,
  Plus,
} from 'lucide-react';
import { useParams, useNavigate } from 'react-router-dom';
import OrgTalkHeader from './OrgTalkHeader';
import CreateChatRoomModal from './CreateChatRoomModal';
import PasswordInputModal from '../pages/PasswordInputModal'; 
import { getOrganizationInfo } from '../service/OrganizationService';
import { getChatRooms, searchChatRooms, joinChatRoom } from '../service/ChatService';
import Pagination from './Pagination';

import ChatRoomCard from './ChatRoomCard';
import SearchFilter from './SearchFilter';

import styles from '../css/ChatRoomsPage.module.css';

const ChatRoomsPage = () => {
  const { orgId } = useParams();
  const navigate = useNavigate();

  // 1) 채팅방 생성 모달 제어
  const [isModalOpen, setIsModalOpen] = useState(false);

  // 2) 조직 정보
  const [organization, setOrganization] = useState(null);

  // 3) 채팅방 목록 + 페이징
  const [chatRooms, setChatRooms] = useState([]);
  const [page, setPage] = useState(0);
  const [size] = useState(9);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);

  // 4) 로딩/에러 (채팅방 조회)
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);

  // 5) 검색 및 필터링
  const [searchQuery, setSearchQuery] = useState('');
  const [filterType, setFilterType] = useState('all');

  // 6) 선택된/호버된 채팅방(UI용)
  const [selectedRoom, setSelectedRoom] = useState(null);
  const [hoveredRoom, setHoveredRoom] = useState(null);

  // 7) "비밀번호 입력" 모달 제어를 위한 상태
  const [showJoinModal, setShowJoinModal] = useState(false);
  const [roomToJoin, setRoomToJoin] = useState(null);     
  const [joinLoading, setJoinLoading] = useState(false);  
  const [joinError, setJoinError] = useState('');

  // 8) 검색 실행을 위한 상태 (엔터 키로만 검색)
  const [activeSearchQuery, setActiveSearchQuery] = useState('');

  // 채팅방 목록 로드 함수 (검색/필터 포함) - 서버 타입 필터링 지원 + 클라이언트 백업 필터링
  const loadChatRooms = useCallback(async (resetPage = false) => {
    if (!orgId) return;

    setIsLoading(true);
    setError(null);

    try {
      const orgIdNum = Number(orgId);
      const currentPage = resetPage ? 0 : page;

      let response;
      if (activeSearchQuery.trim()) {
        // 검색 시 타입 필터 포함
        const params = {
          organizationId: orgIdNum,
          keyword: activeSearchQuery.trim(),
          type: filterType !== 'all' ? filterType.toUpperCase() : undefined,
          page: currentPage,
          size,
          sort: 'createdAt,DESC',
        };

        response = await searchChatRooms(params);
      } else {
        // 일반 목록 조회 시 타입 필터 포함
        const params = {
          organizationId: orgIdNum,
          type: filterType !== 'all' ? filterType.toUpperCase() : undefined,
          page: currentPage,
          size,
          sort: 'createdAt,DESC',
        };

        response = await getChatRooms(params);
      }

      console.log('API Response:', response);
      console.log('Filter Type:', filterType);

      let roomsData = response?.chatRooms || response?.content || [];
      let totalPagesData = response?.totalPages || 0;
      let totalElementsData = response?.totalElements || 0;

      // 서버 필터링이 제대로 동작하지 않는 경우를 위한 클라이언트 백업 필터링
      if (filterType !== 'all') {
        const originalCount = roomsData.length;
        roomsData = roomsData.filter(room => {
          const roomType = room.type?.toLowerCase();
          return roomType === filterType;
        });
        console.log(`Client filtering: ${originalCount} -> ${roomsData.length} rooms`);
        
        // 클라이언트 필터링을 적용한 경우 페이징 정보 재계산
        if (roomsData.length !== originalCount) {
          totalElementsData = roomsData.length; // 임시로 현재 결과만 반영
          totalPagesData = Math.ceil(roomsData.length / size);
        }
      }

      setChatRooms(roomsData);
      setTotalPages(totalPagesData);
      setTotalElements(totalElementsData);
      
      if (resetPage && page !== 0) {
        setPage(0);
      }
      
    } catch (err) {
      console.error('Failed to fetch chat rooms:', err);
      setError('채팅방 목록을 불러오는 중 오류가 발생했습니다.');
      setChatRooms([]);
      setTotalElements(0);
      setTotalPages(0);
    } finally {
      setIsLoading(false);
    }
  }, [orgId, page, size, activeSearchQuery, filterType]);

  // 조직 정보 로드
  useEffect(() => {
    if (!orgId) return;

    const loadOrganization = async () => {
      try {
        const orgIdNum = Number(orgId);
        const data = await getOrganizationInfo(orgIdNum);
        setOrganization(data);
      } catch (err) {
        console.error('Failed to fetch organization info:', err);
      }
    };

    loadOrganization();
  }, [orgId]);

  // 페이지 변경 시 채팅방 목록 로드
  useEffect(() => {
    if (orgId) {
      loadChatRooms();
    }
  }, [page]);

  // 필터 타입 변경 시 즉시 검색
  useEffect(() => {
    if (orgId) {
      loadChatRooms(true);
    }
  }, [filterType]);

  // 실제 검색어 변경 시 검색 실행
  useEffect(() => {
    if (orgId) {
      loadChatRooms(true);
    }
  }, [activeSearchQuery]);

  const handleOpenModal = () => setIsModalOpen(true);
  const handleCloseModal = () => setIsModalOpen(false);

  const handleCreateRoom = ({ id: newRoomId }) => {
    navigate(`/chatroom/${newRoomId}`);
  };

  const handleRequestJoin = (room) => {
    setRoomToJoin(room);
    setJoinError('');
    setShowJoinModal(true);
  };

  const handleJoinSubmit = async (password) => {
    if (!roomToJoin) return;

    setJoinLoading(true);
    setJoinError('');

    try {
      await joinChatRoom({ roomId: roomToJoin.id, password });

      setChatRooms((prevRooms) =>
        prevRooms.map((r) =>
          r.id === roomToJoin.id ? { ...r, joined: true } : r
        )
      );

      setSelectedRoom({ ...roomToJoin, joined: true });
      setShowJoinModal(false);
      setJoinLoading(false);

      navigate(`/chatroom/${roomToJoin.id}`);
    } catch (err) {
      const msg = err.message || '알 수 없는 오류가 발생했습니다.';
      setJoinError(msg);
    } finally {
      setJoinLoading(false);
    }
  };

  const handleJoinClose = () => {
    setJoinError('');
    setShowJoinModal(false);
  };
  
  const handleRoomSelect = (room) => {
    if (room.type === 'PRIVATE' && !room.joined) {
      handleRequestJoin(room);
    } else {
      setSelectedRoom(room);
      navigate(`/chatroom/${room.id}`);
    }
  };

  // 검색어 변경 핸들러 (UI 상태만 변경)
  const handleSearchChange = (query) => {
    setSearchQuery(query);
  };

  // 검색 실행 핸들러 (엔터 키 또는 검색 버튼)
  const handleSearchSubmit = () => {
    setActiveSearchQuery(searchQuery);
  };

  // 검색 초기화 핸들러
  const handleSearchClear = () => {
    setSearchQuery('');
    setActiveSearchQuery('');
  };

  // 필터 타입 변경 핸들러
  const handleFilterChange = (type) => {
    setFilterType(type);
  };

  // 페이지 변경 핸들러
  const handlePageChange = (newPage) => {
    console.log('Page change requested:', newPage);
    if (newPage !== page && newPage >= 0 && newPage < totalPages) {
      setPage(newPage);
    }
  };

  if (isLoading && !organization) {
    return (
      <>
        <OrgTalkHeader />
        <div className={styles['chat-rooms-page']}>
          <div className={styles['background-effects']}>
            <div className={styles['bg-circle-1']}></div>
            <div className={styles['bg-circle-2']}></div>
            <div className={styles['bg-circle-3']}></div>
          </div>
          <div className={styles['main-content']}>
            <div className={styles['chat-rooms-container']}>
              <div className={styles['loading-container']}>
                <MessageCircle size={48} className={styles['loading-icon']} />
                <p className={styles['loading-text']}>채팅방 목록을 불러오는 중...</p>
              </div>
            </div>
          </div>
        </div>
      </>
    );
  }

  return (
    <>
      <OrgTalkHeader />

      <PasswordInputModal
        isOpen={showJoinModal}
        onClose={handleJoinClose}
        onSubmit={handleJoinSubmit}
        roomName={roomToJoin?.name || ''}
        isLoading={joinLoading}
        errorMessage={joinError}
      />

      <div className={styles['chat-rooms-page']}>
        <div className={styles['background-effects']}>
          <div className={styles['bg-circle-1']}></div>
          <div className={styles['bg-circle-2']}></div>
          <div className={styles['bg-circle-3']}></div>
        </div>
        <div className={styles['main-content']}>
          <div className={styles['chat-rooms-container']}>
            <div className={styles['header-section']}>
              <div className={styles['header-top']}>
                <div className={styles['org-info']}>
                  <div className={styles['org-avatar']}>
                    <img
                      src={organization?.avatarUrl}
                      alt={organization?.login}
                      className={styles['org-avatar-img']}
                    />
                  </div>
                  <div className={styles['org-details']}>
                    <h1 className={styles['org-name']}>{organization?.login}</h1>
                    <p className={styles['org-member-count']}>
                      <Users size={16} /> <span>{organization?.memberCount}명의 멤버</span>
                    </p>
                  </div>
                </div>
                <button className={styles['create-button']} onClick={handleOpenModal}>
                  <Plus size={18} />
                  <span>새 채팅방</span>
                </button>
              </div>
              <p className={styles['page-description']}>
                참여하고 싶은 <span className={styles['description-highlight']}>채팅방</span>을 선택하세요
              </p>
            </div>

            <SearchFilter
              searchQuery={searchQuery}
              onSearchChange={handleSearchChange}
              onSearchSubmit={handleSearchSubmit}
              onSearchClear={handleSearchClear}
              filterType={filterType}
              onFilterChange={handleFilterChange}
              totalElements={totalElements}
              filteredElements={chatRooms.length} 
              activeSearchQuery={activeSearchQuery}
              currentPage={page}
              totalPages={totalPages}
            />

            {isLoading && (
              <div className={styles['loading-container']}>
                <MessageCircle size={48} className={styles['loading-icon']} />
                <p className={styles['loading-text']}>검색 중...</p>
              </div>
            )}

            {!isLoading && (
              <div className={styles['rooms-grid']}>
                {chatRooms.map((room) => (
                  <ChatRoomCard
                    key={room.id}
                    room={room}
                    isHovered={hoveredRoom === room.id}
                    isSelected={selectedRoom?.id === room.id}
                    onClick={() => handleRoomSelect(room)}
                    onMouseEnter={() => setHoveredRoom(room.id)}
                    onMouseLeave={() => setHoveredRoom(null)}
                  />
                ))}
              </div>
            )}

            {!isLoading && chatRooms.length === 0 && !error && (
              <div className={styles['empty-state']}>
                <MessageCircle size={48} className={styles['empty-icon']} />
                <h3 className={styles['empty-title']}>
                  {activeSearchQuery ? '검색 결과가 없습니다' : '채팅방이 없습니다'}
                </h3>
                <p className={styles['empty-description']}>
                  {activeSearchQuery
                    ? '다른 검색어로 시도해보거나 전체 목록을 확인해보세요'
                    : '새로운 채팅방을 만들어보세요'}
                </p>
              </div>
            )}

            {error && (
              <div className={styles['error-message']}>
                <p>{error}</p>
              </div>
            )}

            {!isLoading && totalPages > 1 && (
              <div>
                <Pagination
                  currentPage={page}
                  totalPages={totalPages}
                  onPageChange={handlePageChange}
                />
              </div>
            )}

            {/* 실시간 연결 상태 */}
            <div className={styles['status-indicator']}>
              <div className={styles['status-badge']}>
                <div className={styles['status-dot-green']}></div>
                <span className={styles['status-text']}>실시간 연결됨</span>
              </div>
            </div>
          </div>
        </div>

        <div className={styles['bottom-decoration']}></div>

        <CreateChatRoomModal
          isOpen={isModalOpen}
          onClose={handleCloseModal}
          onCreate={handleCreateRoom}
        />
      </div>
    </>
  );
};

export default ChatRoomsPage;