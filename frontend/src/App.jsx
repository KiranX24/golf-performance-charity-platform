import React,{useEffect,useState} from 'react';
import { authApi } from './api/client';
import Layout from './components/Layout';
import Auth from './pages/Auth';
import { Overview, Plans, Scores, Charity, Draws, Winners } from './pages/UserPages';
import { AdminOverview, AdminDraws, AdminWinners, AdminUsers, AdminContent } from './pages/AdminPages';

export default function App(){
 const [user,setUser]=useState(()=>{try{return JSON.parse(localStorage.getItem('dh_user'))}catch{return null}}),[page,setPage]=useState('overview');
 useEffect(()=>{const logout=()=>{setUser(null);setPage('overview')};window.addEventListener('dh:logout',logout);if(localStorage.getItem('dh_token')&&!user)authApi.me().then(u=>{localStorage.setItem('dh_user',JSON.stringify(u));setUser(u)}).catch(()=>logout());return()=>window.removeEventListener('dh:logout',logout)},[]);
 function login(res){localStorage.setItem('dh_token',res.accessToken);localStorage.setItem('dh_user',JSON.stringify(res.user));setUser(res.user);setPage('overview')}
 function logout(){localStorage.removeItem('dh_token');localStorage.removeItem('dh_user');setUser(null)}
 if(!user)return <Auth onLogin={login}/>;
 const admin=user.role==='ROLE_ADMIN';
 const pages={overview:<Overview user={user}/>,plans:<Plans/>,scores:<Scores/>,charity:<Charity/>,draws:<Draws/>,winners:<Winners/>,admin:<AdminOverview/>, 'admin-draws':<AdminDraws/>, 'admin-winners':<AdminWinners/>, 'admin-users':<AdminUsers/>, 'admin-content':<AdminContent/>};
 const activePage=admin?pages[page]:pages[page]||pages.overview;
 return <Layout user={user} page={page} setPage={setPage} onLogout={logout}>{activePage}</Layout>;
}
