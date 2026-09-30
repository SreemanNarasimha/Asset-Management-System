import styles from './Card.module.css';

export default function Card({ title, value, subtitle, children }) {
  return (
    <div className={styles.card}>
      {title && <h3 className={styles.cardTitle}>{title}</h3>}
      {value !== undefined && <p className={styles.cardValue}>{value}</p>}
      {subtitle && <p className={styles.cardSubtitle}>{subtitle}</p>}
      {children && <div className={styles.cardContent}>{children}</div>}
    </div>
  );
}
