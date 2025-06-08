import api from '../lib/axios.ts';

/**
 * 조직 목록 조회 (페이지네이션)
 * @param {number} page - 페이지 번호 (0부터 시작)
 * @param {number} size - 페이지당 항목 수
 * @returns {Promise<PageResponse<OrganizationsResponse>>}
 */
export async function getOrganizations(page = 0, size = 5) {
  try {
    const response = await api.get('/api/member/organizations', {
      params: { page, size },
    });
    return response.data;
  } catch (error) {
    console.error('Failed to fetch organizations:', error);
    throw error;
  }
}
