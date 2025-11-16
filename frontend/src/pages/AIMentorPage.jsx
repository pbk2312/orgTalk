import React, { useState, useRef, useEffect } from 'react';
import { Sparkles, Send, Loader, Code, BookOpen, Lightbulb, ArrowLeft } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import AIChatService from '../service/AIChatService';
import { Prism as SyntaxHighlighter } from 'react-syntax-highlighter';
import { vscDarkPlus } from 'react-syntax-highlighter/dist/esm/styles/prism';
import { toast } from 'react-toastify';
import styles from '../css/AIMentorPage.module.css';

const AIMentorPage = () => {
  const [messages, setMessages] = useState([]);
  const [inputValue, setInputValue] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const messagesEndRef = useRef(null);
  const inputRef = useRef(null);
  const navigate = useNavigate();

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  useEffect(() => {
    // 초기 환영 메시지
    setMessages([
      {
        role: 'assistant',
        content: '안녕하세요! 👋 저는 개발자를 위한 AI 멘토입니다.\n\n개발 관련 질문이나 코드에 대한 궁금증이 있으시면 언제든지 물어보세요!\n\n예시:\n• "React에서 useEffect는 언제 사용하나요?"\n• "Java의 Stream API 사용법을 알려주세요"\n• "RESTful API 설계 원칙을 설명해주세요"',
        timestamp: new Date()
      }
    ]);
  }, []);

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!inputValue.trim() || isLoading) return;

    const userMessage = {
      role: 'user',
      content: inputValue,
      timestamp: new Date()
    };

    setMessages(prev => [...prev, userMessage]);
    setInputValue('');
    setIsLoading(true);

    try {
      const response = await AIChatService.askQuestion(inputValue);
      
      const assistantMessage = {
        role: 'assistant',
        content: response.answer,
        timestamp: new Date(),
        tokensUsed: response.tokensUsed
      };

      setMessages(prev => [...prev, assistantMessage]);
    } catch (error) {
      console.error('AI 응답 실패:', error);
      toast.error('응답을 받는 중 오류가 발생했습니다. 다시 시도해주세요.');
      
      const errorMessage = {
        role: 'assistant',
        content: '죄송합니다. 응답을 받는 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.',
        timestamp: new Date(),
        isError: true
      };
      
      setMessages(prev => [...prev, errorMessage]);
    } finally {
      setIsLoading(false);
      inputRef.current?.focus();
    }
  };

  const handleKeyDown = (e) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleSubmit(e);
    }
  };

  const renderMessageContent = (content) => {
    // 코드 블록을 찾아서 하이라이팅 처리
    const codeBlockRegex = /```(\w+)?\n([\s\S]*?)```/g;
    const parts = [];
    let lastIndex = 0;
    let match;

    while ((match = codeBlockRegex.exec(content)) !== null) {
      // 코드 블록 전의 텍스트
      if (match.index > lastIndex) {
        parts.push({
          type: 'text',
          content: content.substring(lastIndex, match.index)
        });
      }

      // 코드 블록
      parts.push({
        type: 'code',
        language: match[1] || 'text',
        content: match[2].trim()
      });

      lastIndex = match.index + match[0].length;
    }

    // 남은 텍스트
    if (lastIndex < content.length) {
      parts.push({
        type: 'text',
        content: content.substring(lastIndex)
      });
    }

    return parts.map((part, index) => {
      if (part.type === 'code') {
        return (
          <div key={index} className={styles.codeBlock}>
            <div className={styles.codeHeader}>
              <Code size={14} />
              <span>{part.language}</span>
            </div>
            <SyntaxHighlighter
              language={part.language}
              style={vscDarkPlus}
              customStyle={{
                margin: 0,
                borderRadius: '0 0 8px 8px',
                fontSize: '14px'
              }}
            >
              {part.content}
            </SyntaxHighlighter>
          </div>
        );
      }
      return (
        <p key={index} className={styles.messageText}>
          {part.content}
        </p>
      );
    });
  };

  const quickQuestions = [
    { icon: <Code size={18} />, text: "Git 브랜치 전략 설명해주세요" },
    { icon: <BookOpen size={18} />, text: "SOLID 원칙이 뭔가요?" },
    { icon: <Lightbulb size={18} />, text: "Docker와 Kubernetes 차이점은?" }
  ];

  const handleQuickQuestion = (question) => {
    setInputValue(question);
    inputRef.current?.focus();
  };

  return (
    <div className={styles.container}>
      <div className={styles.header}>
        <div className={styles.headerContent}>
          <button 
            className={styles.backButton}
            onClick={() => navigate(-1)}
            title="뒤로가기"
          >
            <ArrowLeft size={24} />
          </button>
          <Sparkles className={styles.headerIcon} />
          <div>
            <h1 className={styles.title}>AI 개발 멘토</h1>
            <p className={styles.subtitle}>개발 관련 질문에 즉시 답변해드립니다</p>
          </div>
        </div>
      </div>

      <div className={styles.chatContainer}>
        <div className={styles.messagesWrapper}>
          {messages.map((message, index) => (
            <div
              key={index}
              className={`${styles.messageContainer} ${
                message.role === 'user' ? styles.userMessage : styles.assistantMessage
              }`}
            >
              <div className={styles.messageContent}>
                {message.role === 'assistant' && (
                  <div className={styles.avatar}>
                    <Sparkles size={20} />
                  </div>
                )}
                <div className={styles.messageBubble}>
                  {renderMessageContent(message.content)}
                  <div className={styles.messageTime}>
                    {message.timestamp.toLocaleTimeString('ko-KR', {
                      hour: '2-digit',
                      minute: '2-digit'
                    })}
                    {message.tokensUsed && (
                      <span className={styles.tokens}> • {message.tokensUsed} tokens</span>
                    )}
                  </div>
                </div>
              </div>
            </div>
          ))}

          {isLoading && (
            <div className={`${styles.messageContainer} ${styles.assistantMessage}`}>
              <div className={styles.messageContent}>
                <div className={styles.avatar}>
                  <Sparkles size={20} />
                </div>
                <div className={styles.messageBubble}>
                  <div className={styles.loadingIndicator}>
                    <Loader className={styles.spinner} />
                    <span>답변을 생성하고 있습니다...</span>
                  </div>
                </div>
              </div>
            </div>
          )}

          <div ref={messagesEndRef} />
        </div>

        {messages.length === 1 && (
          <div className={styles.quickQuestions}>
            <p className={styles.quickQuestionsTitle}>추천 질문</p>
            <div className={styles.quickQuestionsList}>
              {quickQuestions.map((q, index) => (
                <button
                  key={index}
                  className={styles.quickQuestionBtn}
                  onClick={() => handleQuickQuestion(q.text)}
                >
                  {q.icon}
                  <span>{q.text}</span>
                </button>
              ))}
            </div>
          </div>
        )}

        <div className={styles.inputContainer}>
          <form onSubmit={handleSubmit} className={styles.inputForm}>
            <textarea
              ref={inputRef}
              value={inputValue}
              onChange={(e) => setInputValue(e.target.value)}
              onKeyDown={handleKeyDown}
              placeholder="개발 관련 질문을 입력하세요... (Shift + Enter로 줄바꿈)"
              className={styles.input}
              rows={3}
              disabled={isLoading}
            />
            <button
              type="submit"
              className={styles.sendButton}
              disabled={!inputValue.trim() || isLoading}
            >
              {isLoading ? <Loader className={styles.spinner} /> : <Send size={20} />}
            </button>
          </form>
        </div>
      </div>
    </div>
  );
};

export default AIMentorPage;

