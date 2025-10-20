import React, { useEffect, useRef, useState } from 'react';
import { MessageCircle, Users, Plus, Sparkles } from 'lucide-react';
import { useNavigate, useParams } from 'react-router-dom';
import OrgTalkHeader from './OrgTalkHeader';
import CreateChatRoomModal from './CreateChatRoomModal';
import PasswordInputModal from '../pages/PasswordInputModal';
import PublicRoomJoinModal from './PublicRoomJoinModal';
import { getOrganizationInfo } from '../service/OrganizationService';
import { getChatRooms, joinChatRoom, searchChatRooms } from '../service/ChatService';
import Pagination from './Pagination';
import ChatRoomCard from './ChatRoomCard';
import SearchFilter from './SearchFilter';
import styles from '../css/ChatRoomsPage.module.css';

const ChatRoomsPage = () => {
  const { orgId } = useParams();
  const navigate = useNavigate();

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
  const [activeSearchQuery, setActiveSearchQuery] = useState('');

  // modal states
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [showJoinModal, setShowJoinModal] = useState(false);
  const [roomToJoin, setRoomToJoin] = useState(null);
  const [joinLoading, setJoinLoading] = useState(false);
  const [joinError, setJoinError] = useState('');
  const [showPublicJoinModal, setShowPublicJoinModal] = useState(false);
  const [publicRoomToJoin, setPublicRoomToJoin] = useState(null);
  const [publicJoinLoading, setPublicJoinLoading] = useState(false);

  const [selectedRoom, setSelectedRoom] = useState(null);
  const [hoveredRoom, setHoveredRoom] = useState(null);

  // prevent duplicate calls
  const loadingRef = useRef(false);

  // fetch chat rooms
  const fetchChatRooms = async (pageNum, search, filter, options = {}) => {
    if (!orgId || loadingRef.current) return;
    loadingRef.current = true;
    setError(null);
    try {
      const orgIdNum = Number(orgId);
      let response;
      if (search.trim()) {
        response = await searchChatRooms(
          {
            organizationId: orgIdNum,
            keyword: search.trim(),
            type: filter !== 'all' ? filter.toUpperCase() : undefined,
            page: pageNum,
            size,
          },
          options
        );
      } else {
        response = await getChatRooms(
          {
            organizationId: orgIdNum,
            type: filter !== 'all' ? filter.toUpperCase() : undefined,
            page: pageNum,
            size,
            sort: 'createdAt,DESC',
          },
          options
        );
      }
      const rooms = response?.chatRooms || response?.content || [];
      setChatRooms(rooms);
      setTotalPages(response?.totalPages || 0);
      setTotalElements(response?.totalElements || 0);
    } catch (err) {
      if (err.name !== 'AbortError') {
        console.error(err);
        setError('채팅방 목록을 불러오는 중 오류가 발생했습니다.');
      }
    } finally {
      loadingRef.current = false;
    }
  };


  useEffect(() => {
    if (!orgId) return;
    console.log("조직정보 가져오기....")
    const controller = new AbortController();
    const loadOrganization = async () => {
      try {
        const data = await getOrganizationInfo(Number(orgId));
        setOrganization(data);
      } catch (err) {
        if (err.name !== 'AbortError') console.error(err);
      }
    };
    loadOrganization();
    return () => controller.abort();
  }, [orgId]);

  // 2) 페이지, 검색, 필터 변경 시 채팅방 가져오기
  useEffect(() => {
    if (!orgId) return;
    console.log("채팅방 가져오기....")
    const controller = new AbortController();
    const loadChatRooms = async () => {
      setIsLoading(true);
      setError(null);
      await fetchChatRooms(page, activeSearchQuery, filterType, {
        signal: controller.signal,
      });
      setIsLoading(false);
    };
    loadChatRooms();
    return () => controller.abort();
  }, [orgId, page, activeSearchQuery, filterType]);


  const handlePageChange = (newPage) => {
    if (newPage !== page && newPage >= 0 && newPage < totalPages) {
      setPage(newPage);
    }
  };
  const handleFilterChange = (type) => {
    setFilterType(type);
    setPage(0);
  };
  const handleSearchSubmit = () => {
    setActiveSearchQuery(searchQuery);
    setPage(0);
  };
  const handleSearchClear = () => {
    setSearchQuery('');
    setActiveSearchQuery('');
    setPage(0);
  };

  const handleOpenModal = () => setIsModalOpen(true);
  const handleCloseModal = () => setIsModalOpen(false);
  const handleCreateRoom = ({ id }) => navigate(`/chatroom/${id}`);

  const handleRequestJoin = (room) => {
    setRoomToJoin(room);
    setJoinError('');
    setShowJoinModal(true);
  };
  const handleJoinClose = () => setShowJoinModal(false);
  const handleJoinSubmit = async (password) => {
    if (!roomToJoin) return;
    setJoinLoading(true);
    setJoinError('');
    try {
      await joinChatRoom({ roomId: roomToJoin.id, password });
      setChatRooms((prev) =>
        prev.map((r) => (r.id === roomToJoin.id ? { ...r, joined: true } : r))
      );
      navigate(`/chatroom/${roomToJoin.id}`);
    } catch (err) {
      setJoinError(err.message || '알 수 없는 오류가 발생했습니다.');
    } finally {
      setJoinLoading(false);
      setShowJoinModal(false);
    }
  };

  const handlePublicRoomJoinRequest = (room) => {
    setPublicRoomToJoin(room);
    setShowPublicJoinModal(true);
  };
  const handlePublicRoomJoinConfirm = async (room) => {
    setPublicJoinLoading(true);
    try {
      await joinChatRoom({ roomId: room.id });
      setChatRooms((prev) =>
        prev.map((r) => (r.id === room.id ? { ...r, joined: true } : r))
      );
      navigate(`/chatroom/${room.id}`);
    } finally {
      setPublicJoinLoading(false);
      setShowPublicJoinModal(false);
    }
  };
  const handlePublicRoomJoinClose = () => setShowPublicJoinModal(false);

  const handleRoomSelect = (room) => {
    if (room.joined) {
      navigate(`/chatroom/${room.id}`);
    } else if (room.type === 'PRIVATE') {
      handleRequestJoin(room);
    } else {
      handlePublicRoomJoinRequest(room);
    }
  };

  const handleSearchChange = (q) => setSearchQuery(q);

  // initial loading state
  if (isLoading && !organization) {
    return (
      <><OrgTalkHeader />
      <div className={styles['chat-rooms-page']}>
        <div className={styles['loading-container']}>
          <MessageCircle size={48} />
          <p>채팅방 목록을 불러오는 중...</p>
        </div>
      </div></>
    );
  }

    return (
    <>
      <OrgTalkHeader />

      {/* 비밀번호 입력 모달 (비공개방용) */}
      <PasswordInputModal
        isOpen={showJoinModal}
        onClose={handleJoinClose}
        onSubmit={handleJoinSubmit}
        roomName={roomToJoin?.name || ''}
        isLoading={joinLoading}
        errorMessage={joinError}
      />

      {/* 공개방 참여 안내 모달 (새로 추가) */}
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
                      <Users size={16} /> <span>{organization?.memberCount}명의 멤버</span>
                    </p>
                  </div>
                </div>
                <div className={styles['action-buttons']}>
                  <button className={styles['ai-mentor-button']} onClick={() => navigate('/ai-mentor')}>
                    <Sparkles size={18} />
                    <span>AI 멘토</span>
                  </button>
                  <button className={styles['create-button']} onClick={handleOpenModal}>
                    <Plus size={18} />
                    <span>새 채팅방</span>
                  </button>
                </div>
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
