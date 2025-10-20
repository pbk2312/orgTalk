import api from '../lib/axios.ts';

export async function getOrganizationInfo(organizationId) {
  try {
    const response = await api.get(`/organization/${organizationId}`);
    return response.data;
  } catch (error) {
    console.error('Failed to fetch organization info:', error);
    throw error;
  }
}

export async function createOrganization(organizationName) {
  try {
    const response = await api.post('/organization', {
      login: organizationName,
      avatarUrl: null
    });
    return response.data;
  } catch (error) {
    console.error('Failed to create organization:', error);
    throw error;
  }
}