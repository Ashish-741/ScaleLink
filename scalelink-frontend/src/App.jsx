import { Routes, Route, Link, useNavigate } from 'react-router-dom';
import { Link2, LogIn, LayoutDashboard } from 'lucide-react';
import { useState, useEffect } from 'react';

// Placeholder Pages (To be built in Phase 10)
const Home = () => (
  <div className="container slide-up" style={{ textAlign: 'center', paddingTop: '10vh' }}>
    <h1 className="text-gradient" style={{ fontSize: '4rem', marginBottom: '20px' }}>
      Shorten Your Links. Expand Your Reach.
    </h1>
    <p style={{ fontSize: '1.2rem', color: 'var(--text-secondary)', maxWidth: '600px', margin: '0 auto 40px' }}>
      ScaleLink is the premium, high-performance URL shortening platform for modern teams and creators.
    </p>
    <div className="flex-center" style={{ gap: '20px' }}>
      <Link to="/register" className="btn btn-primary" style={{ padding: '15px 30px', fontSize: '1.1rem' }}>
        Get Started Free
      </Link>
    </div>
  </div>
);

const Login = () => <div className="container slide-up" style={{paddingTop: '5vh'}}><h2>Login (Coming soon)</h2></div>;
const Register = () => <div className="container slide-up" style={{paddingTop: '5vh'}}><h2>Register (Coming soon)</h2></div>;
const Dashboard = () => <div className="container slide-up" style={{paddingTop: '5vh'}}><h2>Dashboard (Coming soon)</h2></div>;

function App() {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const navigate = useNavigate();

  // Simple check for token to determine auth state
  useEffect(() => {
    const token = localStorage.getItem('token');
    setIsAuthenticated(!!token);
  }, []);

  const handleLogout = () => {
    localStorage.removeItem('token');
    setIsAuthenticated(false);
    navigate('/');
  };

  return (
    <div className="app-container">
      {/* Background glowing orbs */}
      <div className="bg-glow-1"></div>
      <div className="bg-glow-2"></div>

      {/* Navigation */}
      <nav className="navbar">
        <div className="container flex-between">
          <Link to="/" className="nav-brand">
            <Link2 color="var(--accent-primary)" size={28} />
            ScaleLink
          </Link>
          
          <div className="nav-links">
            {isAuthenticated ? (
              <>
                <Link to="/dashboard" className="btn btn-secondary">
                  <LayoutDashboard size={18} /> Dashboard
                </Link>
                <button onClick={handleLogout} className="btn btn-secondary" style={{border: 'none', background: 'transparent'}}>
                  Logout
                </button>
              </>
            ) : (
              <>
                <Link to="/login" className="btn btn-secondary" style={{border: 'none', background: 'transparent'}}>
                  Sign In
                </Link>
                <Link to="/register" className="btn btn-primary">
                  <LogIn size={18} /> Sign Up
                </Link>
              </>
            )}
          </div>
        </div>
      </nav>

      {/* Main Content Area */}
      <main style={{ flex: 1, paddingBottom: '40px' }}>
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />
          <Route path="/dashboard" element={<Dashboard />} />
        </Routes>
      </main>
    </div>
  );
}

export default App;
