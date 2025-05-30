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
import styles from '../css/ChatRoomsPage.module.css';
import OrgTalkHeader from './OrgTalkHeader';
import { getOrganizationInfo } from '../service/OrganizationService';

// 목 데이터: 채팅방 목록은 그대로 목 데이터로 유지
const mockChatRooms = [
  { id: 1, name: "general", description: "전체 공지사항 및 일반적인 대화", type: "public", memberCount: 127, lastMessage: "새로운 프로젝트 시작해봅시다!", lastMessageTime: "2분 전", unreadCount: 3, isActive: true },
  { id: 2, name: "frontend-dev", description: "프론트엔드 개발 관련 논의", type: "public", memberCount: 45, lastMessage: "React 18의 새로운 기능들에 대해...", lastMessageTime: "15분 전", unreadCount: 12, isActive: true },
  { id: 3, name: "backend-api", description: "백엔드 API 설계 및 개발", type: "private", memberCount: 23, lastMessage: "데이터베이스 스키마 리뷰 완료", lastMessageTime: "1시간 전", unreadCount: 0, isActive: false },
  { id: 4, name: "project-alpha", description: "알파 프로젝트 전용 채널", type: "private", memberCount: 8, lastMessage: "마일스톤 달성 축하합니다! 🎉", lastMessageTime: "3시간 전", unreadCount: 5, isActive: true },
  { id: 5, name: "random", description: "자유로운 대화와 일상 공유", type: "public", memberCount: 89, lastMessage: "점심 뭐 드셨나요?", lastMessageTime: "30분 전", unreadCount: 0, isActive: false },
  { id: 6, name: "code-review", description: "코드 리뷰 및 피드백", type: "public", memberCount: 34, lastMessage: "PR #127 리뷰 요청드립니다", lastMessageTime: "45분 전", unreadCount: 7, isActive: true }
];

const ChatRoomsPage = () => {
  const [chatRooms, setChatRooms] = useState([]);
  const [selectedRoom, setSelectedRoom] = useState(null);
  const [isLoading, setIsLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState("");
  const [filterType, setFilterType] = useState("all");
  const [hoveredRoom, setHoveredRoom] = useState(null);
  const [organization, setOrganization] = useState(null);

  useEffect(() => {
    // 조직 정보 호출
    const loadOrganization = async () => {
      try {
        const orgId = 88020948
        const data = await getOrganizationInfo(orgId);
        setOrganization(data);
      } catch (error) {
        console.error('Failed to fetch organization info:', error);
      }
    };

    // 목 채팅방 불러오기
    const loadChatRooms = async () => {
      setIsLoading(true);
      await new Promise(res => setTimeout(res, 1000));
      setChatRooms(mockChatRooms);
      setIsLoading(false);
    };

    loadOrganization();
    loadChatRooms();
  }, []);

  const filteredRooms = chatRooms.filter(room => {                
    const matchesSearch = [room.name, room.description]
      .some(text => text.toLowerCase().includes(searchQuery.toLowerCase()));
    const matchesFilter = filterType === "all" || room.type === filterType;
    return matchesSearch && matchesFilter;
  });

  const handleRoomSelect = room => {
    setSelectedRoom(room);
    console.log(`Joining room: ${room.name}`);
  };

  const getTypeIcon = type => type === 'private' ? <Lock size={16} /> : <Globe size={16} />;
  const formatTime = time => time;

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
            <div className={styles['header-section']}>
              <div className={styles['org-info']}>
                <div className={styles['org-avatar']}>
                  <img src={organization.avatarUrl} alt={organization.login} className={styles['org-avatar-img']} />
                </div>
                <div className={styles['org-details']}>
                  <h1 className={styles['org-name']}>{organization.login}</h1>
                  <p className={styles['org-member-count']}>
                    <Users size={16} /> <span>{organization.memberCount}명의 멤버</span>
                  </p>
                </div>
              </div>
              <p className={styles['page-description']}>
                참여하고 싶은 <span className={styles['description-highlight']}>채팅방</span>을 선택하세요
              </p>
            </div>
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
                    className={
                      `${styles['filter-button']} ${filterType === type ? styles.active : ''}`
                    }
                  >
                    {type === 'public' ? <><Globe size={16} /> 공개</> : type === 'private' ? <><Lock size={16} /> 비공개</> : '전체'}
                  </button>
                ))}
              </div>
            </div>
            <div className={styles['rooms-grid']}>
              {filteredRooms.map(room => (
                <div
                  key={room.id}
                  onClick={() => handleRoomSelect(room)}
                  onMouseEnter={() => setHoveredRoom(room.id)}
                  onMouseLeave={() => setHoveredRoom(null)}
                  className={
                    `${styles['room-card']} ${hoveredRoom === room.id ? styles.hovered : ''} ${selectedRoom?.id === room.id ? styles.selected : ''}`
                  }
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
                    {room.unreadCount > 0 && <div className={styles['unread-badge']}>{room.unreadCount}</div>}
                  </div>
                  <div className={styles['room-card-body']}>
                    <div className={styles['last-message']}>
                      <p className={styles['last-message-text']}>{room.lastMessage}</p>
                      <div className={styles['message-time']}><Clock size={12} /><span>{formatTime(room.lastMessageTime)}</span></div>
                    </div>
                  </div>
                  <div className={styles['room-card-footer']}>
                    <div className={styles['member-count']}><Users size={16} /><span>{room.memberCount}명</span></div>
                    <div className={styles['room-status']}>
                      <div className={`${styles['status-dot']} ${room.isActive ? styles.active : styles.inactive}`}></div>
                      <span className={styles['status-text']}>{room.isActive ? '활성' : '비활성'}</span>
                    </div>
                    <ChevronRight size={16} className={styles['enter-icon']} />
                  </div>
                </div>
              ))}
            </div>
            {filteredRooms.length === 0 && !isLoading && (
              <div className={styles['empty-state']}>
                <MessageCircle size={48} className={styles['empty-icon']} />
                <h3 className={styles['empty-title']}>{searchQuery ? '검색 결과가 없습니다' : '채팅방이 없습니다'}</h3>
                <p className={styles['empty-description']}>{searchQuery ? '다른 검색어로 시도해보세요' : '새로운 채팅방을 만들어보세요'}</p>
              </div>
            )}
            <div className={styles['create-button-container']}>
              <button className={styles['create-button']}><Plus size={20} /><span>새 채팅방 만들기</span></button>
            </div>
            <div className={styles['status-indicator']}>
              <div className={styles['status-badge']}><div className={styles['status-dot-green']}></div><span className={styles['status-text']}>실시간 연결됨</span></div>
            </div>
          </div>
        </div>
        <div className={styles['bottom-decoration']}></div>
      </div>
    </>
  );
};

export default ChatRoomsPage;