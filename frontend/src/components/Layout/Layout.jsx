import { Link, useLocation } from 'react-router-dom';
import styles from './Layout.module.css';

export default function Layout({ auth, logout, children }) {
  const location = useLocation();
  const isAdmin = auth?.role === 'ADMIN';
  const isBaseCommander = auth?.role === 'BASE_COMMANDER';
  const isLogisticsOfficer = auth?.role === 'LOGISTICS_OFFICER';

  const isActive = (path) => location.pathname === path;

  return (
    <div className={styles.layoutContainer}>
      <aside className={styles.sidebar}>
        <div className={styles.sidebarHeader}>
          <h2 className={styles.logo}>Command Center</h2>
          <div className={styles.userInfo}>
            <span className={styles.username}>{auth?.username}</span>
            <span className={styles.roleBadge}>{auth?.role?.replace('_', ' ')}</span>
          </div>
        </div>

        <nav className={styles.navigation}>
          <Link to="/" className={`${styles.navItem} ${isActive('/') ? styles.active : ''}`}>
            Dashboard
          </Link>
          
          {isAdmin && (
            <div className={styles.navGroup}>
              <h3 className={styles.groupTitle}>Administration</h3>
              <Link to="/bases" className={`${styles.navItem} ${isActive('/bases') ? styles.active : ''}`}>Bases</Link>
              <Link to="/users" className={`${styles.navItem} ${isActive('/users') ? styles.active : ''}`}>System Users</Link>
              <Link to="/equipment-types" className={`${styles.navItem} ${isActive('/equipment-types') ? styles.active : ''}`}>Equipment Types</Link>
            </div>
          )}
          
          {(isAdmin || isBaseCommander) && (
            <div className={styles.navGroup}>
              <h3 className={styles.groupTitle}>Command</h3>
              <Link to="/personnel" className={`${styles.navItem} ${isActive('/personnel') ? styles.active : ''}`}>Personnel</Link>
            </div>
          )}
          
          {(isAdmin || isLogisticsOfficer || isBaseCommander) && (
            <div className={styles.navGroup}>
              <h3 className={styles.groupTitle}>Operations</h3>
              <Link to="/transfers" className={`${styles.navItem} ${isActive('/transfers') ? styles.active : ''}`}>Transfers</Link>
              <Link to="/assignments" className={`${styles.navItem} ${isActive('/assignments') ? styles.active : ''}`}>Assignments</Link>
            </div>
          )}
          
          {(isAdmin || isLogisticsOfficer) && (
            <div className={styles.navGroup}>
              <h3 className={styles.groupTitle}>Procurement</h3>
              <Link to="/purchases" className={`${styles.navItem} ${isActive('/purchases') ? styles.active : ''}`}>Purchases</Link>
              <Link to="/expenditures" className={`${styles.navItem} ${isActive('/expenditures') ? styles.active : ''}`}>Expenditures</Link>
            </div>
          )}
        </nav>

        <div className={styles.sidebarFooter}>
          <button onClick={logout} className={styles.logoutButton}>
            Sign Out
          </button>
        </div>
      </aside>

      <main className={styles.mainContent}>
        <div className={styles.topbar}>
          <h1>Military Asset Management System</h1>
        </div>
        <div className={styles.pageContent}>
          {children}
        </div>
      </main>
    </div>
  );
}
