import axios from 'axios';

const API_BASE_URL = '/api/notification/email-history/filter';

export const fetchEmailHistory = async (filters) => {
    const response = await axios.get(API_BASE_URL, { params: filters });
    return response.data;
};