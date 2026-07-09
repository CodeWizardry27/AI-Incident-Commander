import { useState } from 'react'
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom'
import Dashboard from './pages/Dashboard'
import Investigation from './pages/Investigation'
import Report from './pages/Report'
import './App.css'

function App() {
  return (
    <Router>
      <div className="app">
        <nav className="navbar">
          <a href="/" className="logo">⚡ Incident Commander</a>
          <div className="nav-links">
            <a href="/">Dashboard</a>
          </div>
        </nav>
        <main className="main-content">
          <Routes>
            <Route path="/" element={<Dashboard />} />
            <Route path="/investigate/:id" element={<Investigation />} />
            <Route path="/report/:id" element={<Report />} />
          </Routes>
        </main>
      </div>
    </Router>
  )
}

export default App
