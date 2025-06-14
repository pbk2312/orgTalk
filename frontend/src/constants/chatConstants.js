// src/constants/chatConstants.js


export const supportedLanguages = [
  { value: 'javascript', label: 'JavaScript' },
  { value: 'python', label: 'Python' },
  { value: 'java', label: 'Java' },
  { value: 'cpp', label: 'C++' },
  { value: 'c', label: 'C' },
  { value: 'csharp', label: 'C#' },
  { value: 'php', label: 'PHP' },
  { value: 'ruby', label: 'Ruby' },
  { value: 'go', label: 'Go' },
  { value: 'rust', label: 'Rust' },
  { value: 'typescript', label: 'TypeScript' },
  { value: 'html', label: 'HTML' },
  { value: 'css', label: 'CSS' },
  { value: 'sql', label: 'SQL' },
  { value: 'bash', label: 'Bash' },
  { value: 'json', label: 'JSON' },
  { value: 'xml', label: 'XML' },
  { value: 'yaml', label: 'YAML' }
];


export const languageColors = {
  javascript: '#f7df1e',
  python: '#3776ab',
  java: '#ed8b00',
  cpp: '#00599c',
  c: '#a8b9cc',
  csharp: '#239120',
  php: '#777bb4',
  ruby: '#cc342d',
  go: '#00add8',
  rust: '#ce422b',
  typescript: '#3178c6',
  html: '#e34f26',
  css: '#1572b6',
  sql: '#336791',
  bash: '#4eaa25',
  json: '#292929',
  xml: '#ff6600',
  yaml: '#cb171e'
};

export function getLanguageColor(language) {
  return languageColors[language] || '#6b7280';
}