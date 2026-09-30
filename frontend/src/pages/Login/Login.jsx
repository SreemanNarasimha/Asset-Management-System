import { useState } from 'react';
import axios from 'axios';
import styles from './Login.module.css';

const API_BASE = import.meta.env.VITE_API_BASE || 'http://localhost:8080/api';

export default function Login({ setAuth }) {
  const [credentials, setCredentials] = useState({ username: '', password: '' });
  const [error, setError] = useState(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setCredentials(prev => ({ ...prev, [name]: value }));
  };

  const handleLogin = async (e) => {
    e.preventDefault();
    setIsSubmitting(true);
    setError(null);
    
    try {
      const response = await axios.post(`${API_BASE}/auth/login`, credentials);
      localStorage.setItem('token', response.data.token);
      setAuth(response.data);
    } catch (err) {
      const message = err.response?.data?.message || 'Authentication failed. Please verify your credentials.';
      setError(message);
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className={styles.loginWrapper}>
      <div className={styles.loginCard}>
        <div className={styles.loginHeader}>
          <div className={styles.logoBadge}>MAMS</div>
          <h2>System Authentication</h2>
          <p>Military Asset Management System</p>
        </div>
        
        {error && (
          <div className={styles.errorAlert}>
            <span>⚠️</span> {error}
          </div>
        )}
        
        <form onSubmit={handleLogin} className={styles.loginForm}>
          <div className={styles.formGroup}>
            <label htmlFor="username">Username / Service ID</label>
            <input 
              id="username"
              name="username"
              type="text" 
              value={credentials.username} 
              onChange={handleChange} 
              required 
              disabled={isSubmitting}
              autoComplete="username"
            />
          </div>
          
          <div className={styles.formGroup}>
            <label htmlFor="password">Security Passcode</label>
            <input 
              id="password"
              name="password"
              type="password" 
              value={credentials.password} 
              onChange={handleChange} 
              required 
              disabled={isSubmitting}
              autoComplete="current-password"
            />
          </div>
          
          <button 
            type="submit" 
            className={styles.submitBtn}
            disabled={isSubmitting}
          >
            {isSubmitting ? 'Authenticating...' : 'Secure Login'}
          </button>
        </form>
      </div>
    </div>
  );
}
