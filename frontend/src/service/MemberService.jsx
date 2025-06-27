import api from '../lib/axios.ts';


export async function getOrganizations(page = 0, size = 3) {
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
