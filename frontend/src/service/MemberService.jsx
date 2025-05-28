// src/services/organizationService.js
import api from '../lib/axios.ts';

export async function getOrganizations() {
  try {
    const response = await api.get('/api/member/organizations');
    return response.data;  
  } catch (error) {
    console.error('Failed to fetch organizations:', error);
    throw error;
  }
}