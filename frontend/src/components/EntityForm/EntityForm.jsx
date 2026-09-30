import { useState } from 'react';
import axios from 'axios';
import styles from './EntityForm.module.css';

const API_BASE = 'http://localhost:8080/api';

export default function EntityForm({ title, endpoint, fields, description }) {
  const [formData, setFormData] = useState({});
  const [status, setStatus] = useState({ type: null, message: null });
  const [isSubmitting, setIsSubmitting] = useState(false);

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
  };

  const handleFormSubmit = async (e) => {
    e.preventDefault();
    setIsSubmitting(true);
    setStatus({ type: null, message: null });

    try {
      const payload = {};
      for (const fieldName in formData) {
        if (fieldName.includes('.')) {
          const [parent, child] = fieldName.split('.');
          if (!payload[parent]) payload[parent] = {};
          payload[parent][child] = formData[fieldName];
        } else {
          payload[fieldName] = formData[fieldName];
        }
      }

      await axios.post(`${API_BASE}/${endpoint}`, payload);
      
      setStatus({ 
        type: 'success', 
        message: `${title} record has been successfully created.` 
      });
      
      setFormData({});
      e.target.reset();
      
      setTimeout(() => setStatus({ type: null, message: null }), 5000);
    } catch (err) {
      console.error(`Error submitting ${title}`, err);
      const errorMessage = err.response?.data?.message || `Failed to create ${title.toLowerCase()} record. Please check your inputs.`;
      setStatus({ type: 'error', message: errorMessage });
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className={styles.formContainer}>
      <div className={styles.formHeader}>
        <h2>{title}</h2>
        {description && <p className={styles.formDescription}>{description}</p>}
      </div>

      <div className={styles.formCard}>
        {status.type === 'error' && (
          <div className={`${styles.alert} ${styles.alertError}`}>
            <span className={styles.alertIcon}>⚠️</span>
            {status.message}
          </div>
        )}

        {status.type === 'success' && (
          <div className={`${styles.alert} ${styles.alertSuccess}`}>
            <span className={styles.alertIcon}>✓</span>
            {status.message}
          </div>
        )}

        <form onSubmit={handleFormSubmit} className={styles.gridForm}>
          {fields.map(field => (
            <div key={field.name} className={styles.fieldGroup}>
              <label htmlFor={field.name}>{field.label}</label>
              
              {field.type === 'select' ? (
                <select 
                  id={field.name}
                  name={field.name} 
                  required 
                  onChange={handleInputChange}
                  value={formData[field.name] || ''}
                  disabled={isSubmitting}
                >
                  <option value="" disabled>Select {field.label}</option>
                  {field.options.map(opt => (
                    <option key={opt.value || opt} value={opt.value || opt}>
                      {opt.label || opt}
                    </option>
                  ))}
                </select>
              ) : (
                <input 
                  id={field.name}
                  name={field.name} 
                  placeholder={`Enter ${field.label.toLowerCase()}`} 
                  required 
                  type={field.type || 'text'} 
                  onChange={handleInputChange}
                  value={formData[field.name] || ''}
                  disabled={isSubmitting}
                />
              )}
            </div>
          ))}
          
          <div className={styles.formActions}>
            <button 
              type="submit" 
              className={styles.submitBtn}
              disabled={isSubmitting}
            >
              {isSubmitting ? 'Processing...' : 'Submit Record'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
