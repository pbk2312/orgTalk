import api from '../lib/axios.ts';

export async function getOrganizationInfo(organizationId) {
  try {
    const response = await api.get(`/api/organization/${organizationId}`);
    return response.data;
  } catch (error) {
    console.error('Failed to fetch organization info:', error);
    throw error;
  }
}
