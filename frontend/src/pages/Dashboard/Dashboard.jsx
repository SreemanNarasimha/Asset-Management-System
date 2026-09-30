import { useState, useEffect } from 'react';
import axios from 'axios';
import Card from '../../components/Card/Card';
import styles from './Dashboard.module.css';

const API_BASE = 'http://localhost:8080/api';

export default function Dashboard({ auth }) {
  const [metrics, setMetrics] = useState({ available: 0, assigned: 0, transfers: 0 });
  const [auditLogs, setAuditLogs] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchDashboardMetrics = async () => {
      try {
        setIsLoading(true);
        const response = await axios.get(`${API_BASE}/dashboard/summary`);
        setMetrics(response.data);
        if (auth?.role === 'ADMIN') {
          const auditRes = await axios.get(`${API_BASE}/audit-logs`);
          setAuditLogs(auditRes.data);
        }
        setError(null);
      } catch (err) {
        console.error('Failed to load dashboard metrics', err);
        setError('Could not load dashboard data. Please try again later.');
      } finally {
        setIsLoading(false);
      }
    };

    fetchDashboardMetrics();
    
    // Auto-refresh audit logs every 5 seconds for real-time feel
    let interval;
    if (auth?.role === 'ADMIN') {
      interval = setInterval(async () => {
        try {
          const res = await axios.get(`${API_BASE}/audit-logs`);
          setAuditLogs(res.data);
        } catch (e) {
          // silent fail for background refresh
        }
      }, 5000);
    }
    return () => clearInterval(interval);
  }, [auth?.baseId, auth?.role]);

  const dashboardTitle = auth?.role === 'ADMIN' 
    ? 'Global Command Dashboard' 
    : `Base Operations (ID: ${auth?.baseId})`;

  if (isLoading) {
    return (
      <div className={styles.loadingContainer}>
        <div className={styles.spinner}></div>
        <p>Loading command dashboard...</p>
      </div>
    );
  }

  if (error) {
    return (
      <div className={styles.errorContainer}>
        <h3>System Error</h3>
        <p>{error}</p>
        <button onClick={() => window.location.reload()} className={styles.retryBtn}>
          Retry Connection
        </button>
      </div>
    );
  }

  return (
    <div className={styles.dashboardContainer}>
      <header className={styles.header}>
        <h2>{dashboardTitle}</h2>
        <p className={styles.subtitle}>Overview of military assets and operational metrics</p>
      </header>
      
      <div className={styles.metricsGrid}>
        <Card 
          title="Opening Balance" 
          value={metrics.openingBalance || 0}
          subtitle="Initial assets" 
        />
        <Card 
          title="Net Movement 🛈" 
          value={metrics.netMovement || 0}
          subtitle="Purchases + Transfers In - Transfers Out" 
        />
        <Card 
          title="Closing Balance" 
          value={metrics.closingBalance || 0}
          subtitle="Final available assets" 
        />
        <Card 
          title="Assigned Assets" 
          value={metrics.assigned || 0}
          subtitle="Currently deployed" 
        />
        <Card 
          title="Expended Assets" 
          value={metrics.expended || 0}
          subtitle="Consumed assets" 
        />
      </div>

      {auth?.role === 'ADMIN' && (
        <div className={styles.auditContainer} style={{ marginTop: '2rem' }}>
          <h3 style={{ borderBottom: '1px solid #ccc', paddingBottom: '0.5rem' }}>Real-time Audit Log</h3>
          <div style={{ maxHeight: '400px', overflowY: 'auto', background: '#f9f9f9', padding: '1rem', borderRadius: '8px' }}>
            <table style={{ width: '100%', textAlign: 'left', borderCollapse: 'collapse' }}>
              <thead>
                <tr>
                  <th style={{ padding: '0.5rem', borderBottom: '2px solid #ddd' }}>Timestamp</th>
                  <th style={{ padding: '0.5rem', borderBottom: '2px solid #ddd' }}>User</th>
                  <th style={{ padding: '0.5rem', borderBottom: '2px solid #ddd' }}>Action</th>
                  <th style={{ padding: '0.5rem', borderBottom: '2px solid #ddd' }}>Details</th>
                </tr>
              </thead>
              <tbody>
                {auditLogs.map(log => (
                  <tr key={log.id} style={{ borderBottom: '1px solid #eee' }}>
                    <td style={{ padding: '0.5rem' }}>{new Date(log.timestamp).toLocaleString()}</td>
                    <td style={{ padding: '0.5rem' }}>{log.username}</td>
                    <td style={{ padding: '0.5rem' }}>{log.action}</td>
                    <td style={{ padding: '0.5rem' }}>{log.details}</td>
                  </tr>
                ))}
                {auditLogs.length === 0 && (
                  <tr>
                    <td colSpan="4" style={{ padding: '1rem', textAlign: 'center' }}>No recent activity.</td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </div>
  );
}
