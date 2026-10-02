import React, { useEffect, useState } from 'react';
import { Button } from './UI';

const userLinks = [
  ['overview', 'Overview', '⌂'],
  ['plans', 'Plans', '◇'],
  ['scores', 'Scores', '▣'],
  ['charity', 'Charity', '♥'],
  ['draws', 'Draws', '◈'],
  ['winners', 'Winners', '★']
];

const adminLinks = [
  ['admin', 'Admin overview', '▦'],
  ['admin-draws', 'Draw control', '◈'],
  ['admin-winners', 'Winners & payouts', '★'],
  ['admin-users', 'Users', '♙'],
  ['admin-content', 'Plans & charities', '✦']
];

export default function Layout({
  user,
  page,
  setPage,
  onLogout,
  children
}) {
  const [menuOpen, setMenuOpen] = useState(false);

  const isAdmin = user?.role === 'ROLE_ADMIN';

  const links = isAdmin
    ? [...userLinks, ...adminLinks]
    : userLinks;

  const displayName = isAdmin
    ? 'GOLF ADMIN'
    : user?.fullName || 'User';

  // Close menu when page changes
  useEffect(() => {
    setMenuOpen(false);
  }, [page]);

  // Close menu with Escape key
  useEffect(() => {
    const handleEscape = (event) => {
      if (event.key === 'Escape') {
        setMenuOpen(false);
      }
    };

    document.addEventListener('keydown', handleEscape);

    return () => {
      document.removeEventListener('keydown', handleEscape);
    };
  }, []);

  // Prevent background scrolling while mobile drawer is open
  useEffect(() => {
    if (menuOpen) {
      document.body.style.overflow = 'hidden';
    } else {
      document.body.style.overflow = '';
    }

    return () => {
      document.body.style.overflow = '';
    };
  }, [menuOpen]);

  const handleNavigation = (id) => {
    setPage(id);
    setMenuOpen(false);
  };

  const handleLogout = () => {
    setMenuOpen(false);
    onLogout();
  };

  return (
    <>
      <style>{`

        /* =========================================
           APP LAYOUT
        ========================================= */

        .app-shell {
          min-height: 100vh;
          width: 100%;
          display: flex;
          background: #f5f7f5;
        }

        /* =========================================
           DESKTOP SIDEBAR
        ========================================= */

        .sidebar {
          width: 250px;
          min-width: 250px;
          min-height: 100vh;

          position: fixed;
          left: 0;
          top: 0;
          bottom: 0;

          display: flex;
          flex-direction: column;

          background: #10291d;
          color: #dce8df;

          padding: 25px 16px;

          z-index: 1000;

          overflow-y: auto;
        }

        .logo {
          display: flex;
          gap: 10px;
          align-items: center;
          padding: 4px 10px 30px;
          color: white;
        }

        .logo-mark {
          width: 35px;
          height: 35px;
          min-width: 35px;

          border-radius: 11px;

          background: #c9f25d;
          color: #10291d;

          display: grid;
          place-items: center;

          font-weight: 800;
          font-family: Manrope, sans-serif;
        }

        .logo b,
        .logo strong {
          display: block;
          letter-spacing: .12em;
          font-size: 11px;
          line-height: 1.4;
        }

        .logo strong {
          font-size: 14px;
          color: #c9f25d;
        }

        .sidebar-label {
          font-size: 10px;
          text-transform: uppercase;
          letter-spacing: .14em;
          color: #809689;
          padding: 0 12px 10px;
        }

        .sidebar nav {
          display: grid;
          gap: 5px;
        }

        .nav-item {
          width: 100%;

          border: 0;
          background: transparent;

          color: #aebeb3;

          text-align: left;

          padding: 12px;

          border-radius: 11px;

          display: flex;
          gap: 11px;
          align-items: center;

          font-weight: 600;

          transition:
            background .2s ease,
            color .2s ease;
        }

        .nav-item span {
          width: 24px;
          min-width: 24px;

          display: flex;
          align-items: center;
          justify-content: center;

          font-size: 17px;
        }

        .nav-item:hover {
          background: #1c4430;
          color: #fff;
        }

        .nav-item.active {
          background: #1c4430;
          color: #fff;

          box-shadow: inset 3px 0 #c9f25d;
        }

        /* =========================================
           SIDEBAR USER
        ========================================= */

        .sidebar-bottom {
          margin-top: auto;

          border-top: 1px solid #2b4436;

          padding-top: 16px;
        }

        .mini-user {
          display: flex;
          align-items: center;

          gap: 10px;

          margin-bottom: 12px;

          min-width: 0;
        }

        .mini-user > div:last-child {
          min-width: 0;
        }

        .mini-user b,
        .mini-user small {
          display: block;
        }

        .mini-user b {
          font-size: 13px;

          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }

        .mini-user small {
          color: #82978a;
          font-size: 11px;
          margin-top: 2px;
        }

        /* =========================================
           MAIN CONTENT
        ========================================= */

        .main-content {
          margin-left: 250px;

          width: calc(100% - 250px);

          min-height: 100vh;

          padding-bottom: 30px;
        }

        /* =========================================
           TOPBAR
        ========================================= */

        .topbar {
          height: 72px;

          background: #fff;

          border-bottom: 1px solid #e3e9e3;

          display: flex;
          align-items: center;

          padding: 0 38px;

          position: sticky;
          top: 0;

          z-index: 900;
        }

        .top-spacer {
          flex: 1;
        }

        .user-chip {
          display: flex;
          align-items: center;

          gap: 10px;

          font-weight: 600;
          font-size: 13px;
        }

        .avatar {
          width: 35px;
          height: 35px;

          min-width: 35px;

          border-radius: 50%;

          background: #d9e9d9;
          color: #205336;

          display: grid;
          place-items: center;

          font-weight: 800;
        }

        .avatar.small {
          width: 31px;
          height: 31px;

          min-width: 31px;

          font-size: 12px;
        }

        /* =========================================
           MOBILE BRAND
        ========================================= */

        .mobile-brand {
          display: none;

          color: #123b2a;

          font-size: 12px;

          font-weight: 800;

          letter-spacing: .08em;

          line-height: 1.35;
        }

        .mobile-brand b {
          color: #3c7450;
        }

        /* =========================================
           HAMBURGER BUTTON
        ========================================= */

        .mobile-menu-button {
          display: none;

          width: 42px;
          height: 42px;

          border: 0;
          border-radius: 10px;

          background: #eaf1eb;
          color: #174b31;

          align-items: center;
          justify-content: center;

          flex-direction: column;

          gap: 4px;

          flex-shrink: 0;
        }

        .mobile-menu-button span {
          display: block;

          width: 20px;
          height: 2px;

          border-radius: 4px;

          background: currentColor;

          transition:
            transform .2s ease,
            opacity .2s ease;
        }

        .mobile-menu-button.open span:nth-child(1) {
          transform: translateY(6px) rotate(45deg);
        }

        .mobile-menu-button.open span:nth-child(2) {
          opacity: 0;
        }

        .mobile-menu-button.open span:nth-child(3) {
          transform: translateY(-6px) rotate(-45deg);
        }

        /* =========================================
           MOBILE OVERLAY
        ========================================= */

        .mobile-overlay {
          display: none;

          position: fixed;
          inset: 0;

          background: rgba(6, 25, 16, .48);

          z-index: 1500;

          opacity: 0;

          transition: opacity .25s ease;
        }

        .mobile-overlay.show {
          display: block;
          opacity: 1;
        }

        /* =========================================
           MOBILE DRAWER
        ========================================= */

        .mobile-drawer {
          display: none;

          position: fixed;

          left: 0;
          top: 0;
          bottom: 0;

          width: min(320px, 85vw);

          background: #10291d;

          color: #dce8df;

          z-index: 1600;

          padding: 22px 16px;

          overflow-y: auto;

          transform: translateX(-105%);

          transition: transform .25s ease;

          box-shadow: 12px 0 40px rgba(0, 0, 0, .18);
        }

        .mobile-drawer.open {
          transform: translateX(0);
        }

        .drawer-header {
          display: flex;

          align-items: center;

          justify-content: space-between;

          padding: 4px 6px 24px;

          border-bottom: 1px solid rgba(255,255,255,.08);
        }

        .drawer-brand {
          display: flex;
          align-items: center;

          gap: 10px;

          min-width: 0;
        }

        .drawer-brand-text {
          min-width: 0;
        }

        .drawer-brand-text b,
        .drawer-brand-text strong {
          display: block;

          font-size: 10px;

          letter-spacing: .1em;

          line-height: 1.4;
        }

        .drawer-brand-text strong {
          color: #c9f25d;

          font-size: 12px;
        }

        .drawer-close {
          width: 38px;
          height: 38px;

          border: 0;
          border-radius: 10px;

          background: rgba(255,255,255,.07);

          color: white;

          font-size: 24px;

          display: grid;
          place-items: center;

          flex-shrink: 0;
        }

        .drawer-close:hover {
          background: rgba(255,255,255,.13);
        }

        .drawer-workspace {
          padding: 22px 8px 10px;

          color: #809689;

          font-size: 10px;

          text-transform: uppercase;

          letter-spacing: .14em;

          font-weight: 700;
        }

        .mobile-drawer nav {
          display: grid;
          gap: 5px;
        }

        .mobile-drawer .nav-item {
          min-height: 46px;
        }

        .mobile-drawer .nav-item.active {
          background: #1c4430;
        }

        .drawer-bottom {
          margin-top: 25px;

          padding-top: 18px;

          border-top: 1px solid rgba(255,255,255,.08);
        }

        .drawer-user {
          display: flex;
          align-items: center;

          gap: 10px;

          margin-bottom: 14px;
        }

        .drawer-user-info {
          min-width: 0;
        }

        .drawer-user-info b {
          display: block;

          color: white;

          font-size: 13px;

          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }

        .drawer-user-info small {
          display: block;

          color: #82978a;

          font-size: 11px;

          margin-top: 2px;
        }

        /* =========================================
           HIDE OLD MOBILE BOTTOM NAV
        ========================================= */

        .mobile-bottom-nav {
          display: none !important;
        }

        /* =========================================
           TABLET / SMALL LAPTOP
        ========================================= */

        @media (max-width: 1000px) {

          .sidebar {
            display: none;
          }

          .main-content {
            margin-left: 0;

            width: 100%;

            min-height: 100vh;

            padding-bottom: 30px;
          }

          .mobile-menu-button {
            display: flex;
          }

          .mobile-brand {
            display: block;
          }

          .mobile-drawer {
            display: block;
          }

          .topbar {
            height: 68px;

            padding: 0 20px;

            gap: 14px;
          }

          .user-chip span {
            display: none;
          }

          .top-spacer {
            display: none;
          }

          .user-chip {
            margin-left: auto;
          }
        }

        /* =========================================
           MOBILE
        ========================================= */

        @media (max-width: 600px) {

          .topbar {
            height: 60px;

            padding: 0 14px;

            gap: 10px;
          }

          .mobile-menu-button {
            width: 38px;
            height: 38px;
          }

          .mobile-brand {
            font-size: 10px;

            letter-spacing: .06em;
          }

          .avatar.small {
            width: 34px;
            height: 34px;

            min-width: 34px;

            font-size: 12px;
          }

          .mobile-drawer {
            width: min(310px, 88vw);

            padding: 18px 14px;
          }

          .drawer-header {
            padding-bottom: 20px;
          }

          .drawer-brand-text b {
            font-size: 9px;
          }

          .drawer-brand-text strong {
            font-size: 11px;
          }
        }

        /* =========================================
           VERY SMALL MOBILE
        ========================================= */

        @media (max-width: 380px) {

          .mobile-brand {
            font-size: 9px;

            letter-spacing: .04em;
          }

          .user-chip {
            gap: 6px;
          }

          .mobile-drawer {
            width: 88vw;
          }
        }

      `}</style>

      <div className="app-shell">

        {/* =========================================
            DESKTOP SIDEBAR
        ========================================= */}

        <aside className="sidebar">

          <div className="logo">

            <span className="logo-mark">
              G
            </span>

            <div>
              <b>A GOLF PERFORMANCE</b>
              <strong>& CHARITY DRAW PLATFORM</strong>
            </div>

          </div>

          <div className="sidebar-label">
            Workspace
          </div>

          <nav>

            {links.map(([id, label, icon]) => (

              <button
                key={id}
                className={
                  page === id
                    ? 'nav-item active'
                    : 'nav-item'
                }
                onClick={() => setPage(id)}
              >

                <span>
                  {icon}
                </span>

                {label}

              </button>

            ))}

          </nav>

          <div className="sidebar-bottom">

            <div className="mini-user">

              <div className="avatar">
                {(displayName || 'U')[0].toUpperCase()}
              </div>

              <div>

                <b>
                  {displayName}
                </b>

                <small>
                  {isAdmin
                    ? 'Administrator'
                    : 'Member'}
                </small>

              </div>

            </div>

            <Button
              variant="ghost"
              onClick={onLogout}
            >
              Sign out
            </Button>

          </div>

        </aside>

        {/* =========================================
            MOBILE OVERLAY
        ========================================= */}

        <div
          className={
            menuOpen
              ? 'mobile-overlay show'
              : 'mobile-overlay'
          }
          onClick={() => setMenuOpen(false)}
        />

        {/* =========================================
            MOBILE DRAWER
        ========================================= */}

        <aside
          className={
            menuOpen
              ? 'mobile-drawer open'
              : 'mobile-drawer'
          }
        >

          <div className="drawer-header">

            <div className="drawer-brand">

              <span className="logo-mark">
                G
              </span>

              <div className="drawer-brand-text">

                <b>
                  A GOLF PERFORMANCE
                </b>

                <strong>
                  & CHARITY DRAW PLATFORM
                </strong>

              </div>

            </div>

            <button
              className="drawer-close"
              onClick={() => setMenuOpen(false)}
              aria-label="Close navigation"
            >
              ×
            </button>

          </div>

          <div className="drawer-workspace">
            Workspace
          </div>

          <nav>

            {links.map(([id, label, icon]) => (

              <button
                key={id}
                className={
                  page === id
                    ? 'nav-item active'
                    : 'nav-item'
                }
                onClick={() => handleNavigation(id)}
              >

                <span>
                  {icon}
                </span>

                {label}

              </button>

            ))}

          </nav>

          <div className="drawer-bottom">

            <div className="drawer-user">

              <div className="avatar">

                {(displayName || 'U')[0].toUpperCase()}

              </div>

              <div className="drawer-user-info">

                <b>
                  {displayName}
                </b>

                <small>
                  {isAdmin
                    ? 'Administrator'
                    : 'Member'}
                </small>

              </div>

            </div>

            <Button
              variant="ghost"
              onClick={handleLogout}
            >
              Sign out
            </Button>

          </div>

        </aside>

        {/* =========================================
            MAIN CONTENT
        ========================================= */}

        <main className="main-content">

          <header className="topbar">

            {/* Hamburger */}

            <button
              className={
                menuOpen
                  ? 'mobile-menu-button open'
                  : 'mobile-menu-button'
              }
              onClick={() => setMenuOpen(!menuOpen)}
              aria-label={
                menuOpen
                  ? 'Close navigation'
                  : 'Open navigation'
              }
              aria-expanded={menuOpen}
            >

              <span />
              <span />
              <span />

            </button>

            {/* Mobile Brand */}

            <div className="mobile-brand">

              {isAdmin ? (
                <>
                  GOLF <b>ADMIN</b>
                </>
              ) : (
                <>
                  GOLF <b>PERFORMANCE</b>
                </>
              )}

            </div>

            <div className="top-spacer" />

            {/* User */}

            <div className="user-chip">

              <div className="avatar small">

                {(displayName || 'U')[0].toUpperCase()}

              </div>

              <span>
                {displayName}
              </span>

            </div>

          </header>

          {children}

        </main>

      </div>
    </>
  );
}