import React, { useState, useEffect } from 'react';
import {
  MessageCircle,
  Users,
  Hash,
  Lock,
  Globe,
  ChevronRight,
  Plus,
  Search,
  Clock
} from 'lucide-react';
import { useParams } from 'react-router-dom';
import OrgTalkHeader from './OrgTalkHeader';
import CreateChatRoomModal from './CreateChatRoomModal';
import { getOrganizationInfo } from '../service/OrganizationService';
import { getChatRooms } from '../service/ChatService';
import Pagination from './Pagination';

import styles from '../css/ChatRoomsPage.module.css';

const ChatRoomsPage = () => {
  const { orgId } = useParams();

  // 모달 제어
  const [isModalOpen, setIsModalOpen] = useState(false);

  // 조직 정보
  const [organization, setOrganization] = useState(null);

  // 채팅방 목록 + 페이징
  const [chatRooms, setChatRooms] = useState([]);
  const [page, setPage] = useState(0);            
  const [size] = useState(6);                    
  const [totalPages, setTotalPages] = useState(0);    
  const [totalElements, setTotalElements] = useState(0);

  // 로딩/에러 상태
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);

  // 검색 및 필터링
  const [searchQuery, setSearchQuery] = useState('');
  const [filterType, setFilterType] = useState('all');

  // 선택된 채팅방
  const [selectedRoom, setSelectedRoom] = useState(null);
  const [hoveredRoom, setHoveredRoom] = useState(null);

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
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [orgId, page]);

  const handleOpenModal = () => setIsModalOpen(true);
  const handleCloseModal = () => setIsModalOpen(false);

  const handleCreateRoom = ({ name, description, type }) => {
    const newRoom = {
      id: Date.now(),
      name,
      description,
      type: type.toUpperCase(),
      memberCount: 1,
      messageCount: 0,
      lastMessage: '',
      lastMessageAt: new Date().toISOString(),
      isActive: true
    };
    setChatRooms(prev => [newRoom, ...prev]);
    setSelectedRoom(newRoom);
  };

  const filteredRooms = chatRooms.filter(room => {
    const matchesSearch = [room.name, room.description]
      .some(text => text.toLowerCase().includes(searchQuery.toLowerCase()));
    const matchesFilter =
      filterType === 'all' || room.type.toLowerCase() === filterType;
    return matchesSearch && matchesFilter;
  });

  const handleRoomSelect = room => {
    setSelectedRoom(room);
    console.log(`Joining room: ${room.name}`);
  };

  const getTypeIcon = type =>
    type === 'PRIVATE' ? <Lock size={16} /> : <Globe size={16} />;

  const formatTime = isoString => {
    if (!isoString) return '';
    try {
      return new Date(isoString).toLocaleString();
    } catch {
      return isoString;
    }
  };

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

            {/* —— Search & Filter Section —— */}
            <div className={styles['search-section']}>
              <div className={styles['search-box']}>
                <Search size={20} className={styles['search-icon']} />
                <input
                  type="text"
                  placeholder="채팅방 검색..."
                  value={searchQuery}
                  onChange={e => setSearchQuery(e.target.value)}
                  className={styles['search-input']}
                />
              </div>
              <div className={styles['filter-buttons']}>
                {['all', 'public', 'private'].map(type => (
                  <button
                    key={type}
                    onClick={() => setFilterType(type)}
                    className={`${styles['filter-button']} ${
                      filterType === type ? styles.active : ''
                    }`}
                  >
                    {type === 'public' ? (
                      <>
                        <Globe size={16} /> 공개
                      </>
                    ) : type === 'private' ? (
                      <>
                        <Lock size={16} /> 비공개
                      </>
                    ) : (
                      '전체'
                    )}
                  </button>
                ))}
              </div>
            </div>

            {/* —— 채팅방 목록 Grid —— */}
            <div className={styles['rooms-grid']}>
              {filteredRooms.map(room => (
                <div
                  key={room.id}
                  onClick={() => handleRoomSelect(room)}
                  onMouseEnter={() => setHoveredRoom(room.id)}
                  onMouseLeave={() => setHoveredRoom(null)}
                  className={`${styles['room-card']} ${
                    hoveredRoom === room.id ? styles.hovered : ''
                  } ${selectedRoom?.id === room.id ? styles.selected : ''}`}
                >
                  <div className={styles['room-card-header']}>
                    <div className={styles['room-icon']}><Hash size={20} /></div>
                    <div className={styles['room-info']}>
                      <div className={styles['room-name-row']}>
                        <h3 className={styles['room-name']}>{room.name}</h3>
                        <div className={styles['room-type']}>{getTypeIcon(room.type)}</div>
                      </div>
                      <p className={styles['room-description']}>{room.description}</p>
                    </div>
                  </div>
                  <div className={styles['room-card-body']}>
                    <div className={styles['last-message']}>
                      <p className={styles['last-message-text']}>{room.lastMessage}</p>
                      <div className={styles['message-time']}>
                        <Clock size={12} />
                        <span>{formatTime(room.lastMessageAt)}</span>
                      </div>
                    </div>
                  </div>
                  <div className={styles['room-card-footer']}>
                    <div className={styles['member-count']}>
                      <Users size={16} /> <span>{room.memberCount}명</span>
                    </div>
                    <div className={styles['room-status']}>
                      <div
                        className={`${styles['status-dot']} ${
                          room.isActive ? styles.active : styles.inactive
                        }`}
                      ></div>
                      <span className={styles['status-text']}>
                        {room.isActive ? '활성' : '비활성'}
                      </span>
                    </div>
                    <ChevronRight size={16} className={styles['enter-icon']} />
                  </div>
                </div>
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
                onPageChange={newPage => setPage(newPage)}
              />
            )}

            {/* —— 실시간 상태 표시 —— */}
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