// src/components/ChatRoomsPage.jsx
import React, { useState, useEffect } from 'react';
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
import { getChatRooms, joinChatRoom } from '../service/ChatService';
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
  const [size] = useState(6);
  const [totalPages, setTotalPages] = useState(0);
  /* eslint-disable-next-line no-unused-vars */
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

  // 7) “비밀번호 입력” 모달 제어를 위한 상태
  const [showJoinModal, setShowJoinModal] = useState(false);
  const [roomToJoin, setRoomToJoin] = useState(null);     // 사용자가 입장하려고 시도한 방 객체
  const [joinLoading, setJoinLoading] = useState(false);  // 비밀번호 검증 API 호출 중 상태
  const [joinError, setJoinError] = useState('');         // 비밀번호 틀릴 때 보여줄 에러

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

    const loadChatRooms = async () => {
      setIsLoading(true);
      setError(null);

      try {
        const orgIdNum = Number(orgId);
        const params = {
          organizationId: orgIdNum,
          page,
          size,
          sort: 'lastMessageAt,DESC'
        };

       
        const {
          chatRooms: fetchedRooms,
          page: currentPage,
          totalPages: fetchedTotalPages,
          totalElements: fetchedTotalElements
        } = await getChatRooms(params);

        setChatRooms(fetchedRooms);
        setPage(currentPage);
        setTotalPages(fetchedTotalPages);
        setTotalElements(fetchedTotalElements);
      } catch (err) {
        console.error('Failed to fetch chat rooms:', err);
        setError('채팅방 목록을 불러오는 중 오류가 발생했습니다.');
      } finally {
        setIsLoading(false);
      }
    };

    loadOrganization();
    loadChatRooms();

  }, [orgId,  page, size, setTotalElements]);


  const handleOpenModal = () => setIsModalOpen(true);
  const handleCloseModal = () => setIsModalOpen(false);

  // 생성 모달에서 방 생성 후, 로컬 state에 바로 추가
  const handleCreateRoom = ({ id: newRoomId }) => {
    // 곧바로 채팅룸으로 이ㅇ
    navigate(`/chatroom/${newRoomId}`);
  };

  // ---------- Private 방 입장을 위한 모달 열기 로직 ----------
  const handleRequestJoin = (room) => {
    setRoomToJoin(room);
    setJoinError('');
    setShowJoinModal(true);
  };

  // 모달에서 ‘확인’ 클릭 시 실행
  const handleJoinSubmit = async (password) => {
    if (!roomToJoin) return;

    setJoinLoading(true);
    setJoinError('');

    try {
      await joinChatRoom({ roomId: roomToJoin.id, password });

      // 성공 시 로컬 state ‘joined=true’로 업데이트
      setChatRooms((prevRooms) =>
        prevRooms.map((r) =>
          r.id === roomToJoin.id ? { ...r, joined: true } : r
        )
      );

      setSelectedRoom({ ...roomToJoin, joined: true });
      setShowJoinModal(false);
      setJoinLoading(false);

      // 비밀번호 맞으면 채팅방으로 이동
      navigate(`/chatroom/${roomToJoin.id}`);
    } catch (err) {
      console.error(`Failed to join chat room (roomId: ${roomToJoin.id}):`, err);
      setJoinError('비밀번호가 틀렸거나 입장할 수 없습니다.');
      setJoinLoading(false);
    }
  };

  const handleJoinClose = () => {
    setJoinError('');
    setShowJoinModal(false);
  };
  // ----------------------------------------------------------

  // ---------- 채팅방 클릭 시 분기 처리 ----------
  const handleRoomSelect = (room) => {
    // Private & 미참여 상태라면 ‘비밀번호 입력 모달’ 열기
    if (room.type === 'PRIVATE' && !room.joined) {
      handleRequestJoin(room);
    } else {
      // Public 이거나 이미 joined 상태면 바로 이동
      setSelectedRoom(room);
      navigate(`/chatroom/${room.id}`);
    }
  };
  // ----------------------------------------------------------

  // 검색/필터링된 방 목록
  const filteredRooms = chatRooms.filter((room) => {
    const matchesSearch = [room.name, room.description].some((text) =>
      text?.toLowerCase().includes(searchQuery.toLowerCase())
    );
    const matchesFilter =
      filterType === 'all' || room.type.toLowerCase() === filterType;
    return matchesSearch && matchesFilter;
  });

  // 로딩 중 또는 조직 정보가 없으면 로딩 스피너 화면
  if (isLoading || !organization) {
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
            {/* —— Header Section (조직 정보 + 채팅방 만들기 버튼) —— */}
            <div className={styles['header-section']}>
              <div className={styles['header-top']}>
                <div className={styles['org-info']}>
                  <div className={styles['org-avatar']}>
                    <img
                      src={organization.avatarUrl}
                      alt={organization.login}
                      className={styles['org-avatar-img']}
                    />
                  </div>
                  <div className={styles['org-details']}>
                    <h1 className={styles['org-name']}>{organization.login}</h1>
                    <p className={styles['org-member-count']}>
                      <Users size={16} /> <span>{organization.memberCount}명의 멤버</span>
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
   onSearchChange={setSearchQuery}
   filterType={filterType}
   onFilterChange={setFilterType}
 />

            {/* —— 채팅방 목록 Grid —— */}
            <div className={styles['rooms-grid']}>
              {filteredRooms.map((room) => (
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

            {/* —— 빈 상태 혹은 에러 표시 —— */}
            {filteredRooms.length === 0 && !isLoading && !error && (
              <div className={styles['empty-state']}>
                <MessageCircle size={48} className={styles['empty-icon']} />
                <h3 className={styles['empty-title']}>
                  {searchQuery ? '검색 결과가 없습니다' : '채팅방이 없습니다'}
                </h3>
                <p className={styles['empty-description']}>
                  {searchQuery
                    ? '다른 검색어로 시도해보세요'
                    : '새로운 채팅방을 만들어보세요'}
                </p>
              </div>
            )}

            {error && (
              <div className={styles['error-message']}>
                <p>{error}</p>
              </div>
            )}

            {/* —— Pagination —— */}
            {totalPages > 1 && (
              <Pagination
                currentPage={page}
                totalPages={totalPages}
                onPageChange={(newPage) => setPage(newPage)}
              />
            )}

            {/* —— 실시간 연결 상태 (하단 상시 표시) —— */}
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
