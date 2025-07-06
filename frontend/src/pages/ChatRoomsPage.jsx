// src/components/ChatRoomsPage.jsx
import React, {useCallback, useEffect, useRef, useState} from 'react';
import {MessageCircle, Plus, Users,} from 'lucide-react';
import {useNavigate, useParams} from 'react-router-dom';
import OrgTalkHeader from './OrgTalkHeader';
import CreateChatRoomModal from './CreateChatRoomModal';
import PasswordInputModal from '../pages/PasswordInputModal';
import PublicRoomJoinModal from './PublicRoomJoinModal';
import {getOrganizationInfo} from '../service/OrganizationService';
import {
  getChatRooms,
  joinChatRoom,
  searchChatRooms
} from '../service/ChatService';
import Pagination from './Pagination';

import ChatRoomCard from './ChatRoomCard';
import SearchFilter from './SearchFilter';

import styles from '../css/ChatRoomsPage.module.css';

const ChatRoomsPage = () => {
  const {orgId} = useParams();
  const navigate = useNavigate();

  const [isModalOpen, setIsModalOpen] = useState(false);
  const [organization, setOrganization] = useState(null);
  const [chatRooms, setChatRooms] = useState([]);
  const [page, setPage] = useState(0);
  const [size] = useState(9);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);
  const [searchQuery, setSearchQuery] = useState('');
  const [filterType, setFilterType] = useState('all');
  const [selectedRoom, setSelectedRoom] = useState(null);
  const [hoveredRoom, setHoveredRoom] = useState(null);
  const [showJoinModal, setShowJoinModal] = useState(false);
  const [roomToJoin, setRoomToJoin] = useState(null);
  const [joinLoading, setJoinLoading] = useState(false);
  const [joinError, setJoinError] = useState('');
  const [activeSearchQuery, setActiveSearchQuery] = useState('');
  const [showPublicJoinModal, setShowPublicJoinModal] = useState(false);
  const [publicRoomToJoin, setPublicRoomToJoin] = useState(null);
  const [publicJoinLoading, setPublicJoinLoading] = useState(false);

  // API 호출 방지를 위한 ref
  const loadingRef = useRef(false);
  const mountedRef = useRef(false);

  // 통합된 채팅방 로드 함수
  const loadChatRooms = useCallback(async (options = {}) => {
    if (!orgId || loadingRef.current) {
      return;
    }

    const {
      resetPage = false,
      pageNum = page,
      search = activeSearchQuery,
      filter = filterType
    } = options;

    loadingRef.current = true;
    setIsLoading(true);
    setError(null);

    try {
      const orgIdNum = Number(orgId);
      const currentPage = resetPage ? 0 : pageNum;

      let response;
      if (search.trim()) {
        const params = {
          organizationId: orgIdNum,
          keyword: search.trim(),
          type: filter !== 'all' ? filter.toUpperCase() : undefined,
          page: currentPage,
          size
        };
        response = await searchChatRooms(params);
      } else {
        const params = {
          organizationId: orgIdNum,
          type: filter !== 'all' ? filter.toUpperCase() : undefined,
          page: currentPage,
          size,
          sort: 'createdAt,DESC',
        };
        response = await getChatRooms(params);
      }

      console.log('API Response:', response);
      console.log('Filter Type:', filter);

      const roomsData = response?.chatRooms || response?.content || [];
      const totalPagesData = response?.totalPages || 0;
      const totalElementsData = response?.totalElements || 0;

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
      loadingRef.current = false;
    }
  }, [orgId, page, size, activeSearchQuery, filterType]);

  // 조직 정보 로드
  useEffect(() => {
    if (!orgId) {
      return;
    }

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

  // 초기 로드 및 페이지 변경 시에만 실행
  useEffect(() => {
    if (!orgId || !mountedRef.current) {
      mountedRef.current = true;
      return;
    }

    loadChatRooms({pageNum: page});
  }, [orgId, page]);

  // 필터 변경 시 실행
  useEffect(() => {
    if (!mountedRef.current) {
      return;
    }

    loadChatRooms({
      resetPage: true,
      filter: filterType,
      search: activeSearchQuery
    });
  }, [filterType]);

  // 검색 실행 시 실행
  useEffect(() => {
    if (!mountedRef.current) {
      return;
    }

    loadChatRooms({
      resetPage: true,
      search: activeSearchQuery,
      filter: filterType
    });
  }, [activeSearchQuery]);

  // 컴포넌트 마운트 시 초기 로드
  useEffect(() => {
    if (!orgId) {
      return;
    }

    loadChatRooms();
  }, [orgId]);

  const handleOpenModal = () => setIsModalOpen(true);
  const handleCloseModal = () => setIsModalOpen(false);

  const handleCreateRoom = ({id: newRoomId}) => {
    navigate(`/chatroom/${newRoomId}`);
  };

  const handleRequestJoin = (room) => {
    setRoomToJoin(room);
    setJoinError('');
    setShowJoinModal(true);
  };

  const handleJoinSubmit = async (password) => {
    if (!roomToJoin) {
      return;
    }

    setJoinLoading(true);
    setJoinError('');

    try {
      await joinChatRoom({roomId: roomToJoin.id, password});

      setChatRooms((prevRooms) =>
          prevRooms.map((r) =>
              r.id === roomToJoin.id ? {...r, joined: true} : r
          )
      );

      setSelectedRoom({...roomToJoin, joined: true});
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

  const handlePublicRoomJoinRequest = (room) => {
    setPublicRoomToJoin(room);
    setShowPublicJoinModal(true);
  };

  const handlePublicRoomJoinConfirm = async (room) => {
    setPublicJoinLoading(true);

    try {
      await joinChatRoom({roomId: room.id, password: null});

      setChatRooms((prevRooms) =>
          prevRooms.map((r) =>
              r.id === room.id ? {...r, joined: true} : r
          )
      );

      setSelectedRoom({...room, joined: true});
      setShowPublicJoinModal(false);
      setPublicJoinLoading(false);

      navigate(`/chatroom/${room.id}`);
    } catch (err) {
      console.error('Failed to join public room:', err);
    } finally {
      setPublicJoinLoading(false);
    }
  };

  const handlePublicRoomJoinClose = () => {
    setShowPublicJoinModal(false);
    setPublicRoomToJoin(null);
  };

  const handleRoomSelect = (room) => {
    if (room.joined) {
      setSelectedRoom(room);
      navigate(`/chatroom/${room.id}`);
    } else if (room.type === 'PRIVATE') {
      handleRequestJoin(room);
    } else if (room.type === 'PUBLIC') {
      handlePublicRoomJoinRequest(room);
    }
  };

  const handleSearchChange = (query) => {
    setSearchQuery(query);
  };

  const handleSearchSubmit = () => {
    setActiveSearchQuery(searchQuery);
  };

  const handleSearchClear = () => {
    setSearchQuery('');
    setActiveSearchQuery('');
  };

  const handleFilterChange = (type) => {
    setFilterType(type);
  };

  const handlePageChange = (newPage) => {
    console.log('Page change requested:', newPage);
    if (newPage !== page && newPage >= 0 && newPage < totalPages) {
      setPage(newPage);
    }
  };

  if (isLoading && !organization) {
    return (
        <>
          <OrgTalkHeader/>
          <div className={styles['chat-rooms-page']}>
            <div className={styles['background-effects']}>
              <div className={styles['bg-circle-1']}></div>
              <div className={styles['bg-circle-2']}></div>
              <div className={styles['bg-circle-3']}></div>
            </div>
            <div className={styles['main-content']}>
              <div className={styles['chat-rooms-container']}>
                <div className={styles['loading-container']}>
                  <MessageCircle size={48} className={styles['loading-icon']}/>
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
        <OrgTalkHeader/>

        <PasswordInputModal
            isOpen={showJoinModal}
            onClose={handleJoinClose}
            onSubmit={handleJoinSubmit}
            roomName={roomToJoin?.name || ''}
            isLoading={joinLoading}
            errorMessage={joinError}
        />

        <PublicRoomJoinModal
            isOpen={showPublicJoinModal}
            onClose={handlePublicRoomJoinClose}
            onConfirm={handlePublicRoomJoinConfirm}
            room={publicRoomToJoin}
            isLoading={publicJoinLoading}
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
                        <Users size={16}/> <span>{organization?.memberCount}명의 멤버</span>
                      </p>
                    </div>
                  </div>
                  <button className={styles['create-button']}
                          onClick={handleOpenModal}>
                    <Plus size={18}/>
                    <span>새 채팅방</span>
                  </button>
                </div>
                <p className={styles['page-description']}>
                  참여하고 싶은 <span
                    className={styles['description-highlight']}>채팅방</span>을
                  선택하세요
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
                    <MessageCircle size={48}
                                   className={styles['loading-icon']}/>
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
                    <MessageCircle size={48} className={styles['empty-icon']}/>
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
