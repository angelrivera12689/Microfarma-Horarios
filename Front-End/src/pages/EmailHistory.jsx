import React, { useState, useEffect } from 'react';
import { fetchEmailHistory } from '../services/emailHistoryService';

const EmailHistory = () => {
    const [emails, setEmails] = useState([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const [filters, setFilters] = useState({
        startDate: '',
        endDate: '',
        recipient: '',
        status: ''
    });

    const handleFilterChange = (e) => {
        setFilters({ ...filters, [e.target.name]: e.target.value });
    };

    const loadEmails = async () => {
        setLoading(true);
        setError(null);
        try {
            const data = await fetchEmailHistory(filters);
            setEmails(data);
        } catch (err) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        loadEmails();
    }, [filters]);

    return (
        <div>
            <h1>Email History</h1>
            <form>
                <label>
                    Start Date:
                    <input type="date" name="startDate" value={filters.startDate} onChange={handleFilterChange} />
                </label>
                <label>
                    End Date:
                    <input type="date" name="endDate" value={filters.endDate} onChange={handleFilterChange} />
                </label>
                <label>
                    Recipient:
                    <input type="text" name="recipient" value={filters.recipient} onChange={handleFilterChange} placeholder="Recipient email" />
                </label>
                <label>
                    Status:
                    <select name="status" value={filters.status} onChange={handleFilterChange}>
                        <option value="">All</option>
                        <option value="SENT">Sent</option>
                        <option value="DELIVERED">Delivered</option>
                        <option value="FAILED">Failed</option>
                        <option value="PENDING">Pending</option>
                    </select>
                </label>
            </form>
            {loading && <p>Loading...</p>}
            {error && <p>Error: {error}</p>}
            <table>
                <thead>
                    <tr>
                        <th>Recipient</th>
                        <th>Subject</th>
                        <th>Sent At</th>
                        <th>Delivery Status</th>
                    </tr>
                </thead>
                <tbody>
                    {emails.map((email, index) => (
                        <tr key={index}>
                            <td>{email.recipient}</td>
                            <td>{email.subject}</td>
                            <td>{email.sentAt}</td>
                            <td>{email.deliveryStatus}</td>
                        </tr>
                    ))}
                </tbody>
            </table>
        </div>
    );
};

export default EmailHistory;