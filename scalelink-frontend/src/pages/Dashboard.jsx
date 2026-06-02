import { useState, useEffect } from 'react';
import { api } from '../services/api';
import { Link2, Copy, BarChart2, Plus, ExternalLink } from 'lucide-react';

export default function Dashboard() {
  const [urls, setUrls] = useState([]);
  const [pageInfo, setPageInfo] = useState({ pageNo: 0, totalPages: 1, totalElements: 0 });
  const [isLoading, setIsLoading] = useState(true);
  
  // Create URL Form State
  const [originalUrl, setOriginalUrl] = useState('');
  const [customAlias, setCustomAlias] = useState('');
  const [isCreating, setIsCreating] = useState(false);
  const [createError, setCreateError] = useState('');
  const [createSuccess, setCreateSuccess] = useState(false);

  const fetchUrls = async (page = 0) => {
    setIsLoading(true);
    try {
      const data = await api.getUserUrls(page);
      setUrls(data.content);
      setPageInfo({
        pageNo: data.pageNo,
        totalPages: data.totalPages,
        totalElements: data.totalElements
      });
    } catch (err) {
      console.error('Failed to fetch URLs', err);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchUrls(0);
  }, []);

  const handleCreateUrl = async (e) => {
    e.preventDefault();
    setIsCreating(true);
    setCreateError('');
    setCreateSuccess(false);

    try {
      await api.createShortUrl({
        originalUrl,
        customAlias: customAlias.trim() === '' ? null : customAlias
      });
      setCreateSuccess(true);
      setOriginalUrl('');
      setCustomAlias('');
      fetchUrls(0); // Refresh the list
      
      // Hide success message after 3 seconds
      setTimeout(() => setCreateSuccess(false), 3000);
    } catch (err) {
      setCreateError(err.message);
    } finally {
      setIsCreating(false);
    }
  };

  const copyToClipboard = (text) => {
    navigator.clipboard.writeText(text);
    // You could add a toast notification here in a real app
  };

  return (
    <div className="container slide-up" style={{ paddingTop: '40px' }}>
      
      {/* Top Section: Stats & Create Form */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))', gap: '24px', marginBottom: '40px' }}>
        
        {/* Create URL Form */}
        <div className="glass-panel" style={{ padding: '24px' }}>
          <h3 style={{ marginBottom: '20px', display: 'flex', alignItems: 'center', gap: '8px' }}>
            <Link2 color="var(--accent-primary)" /> Shorten a new URL
          </h3>
          
          {createError && <div style={{ color: 'var(--error)', marginBottom: '16px', fontSize: '0.875rem' }}>{createError}</div>}
          {createSuccess && <div style={{ color: 'var(--success)', marginBottom: '16px', fontSize: '0.875rem' }}>URL created successfully!</div>}
          
          <form onSubmit={handleCreateUrl}>
            <div className="form-group">
              <label className="form-label">Destination URL</label>
              <input 
                type="url" 
                className="form-input" 
                placeholder="https://example.com/very-long-url-path" 
                value={originalUrl}
                onChange={(e) => setOriginalUrl(e.target.value)}
                required
              />
            </div>
            
            <div className="form-group" style={{ marginBottom: '24px' }}>
              <label className="form-label">Custom Alias (Optional)</label>
              <div style={{ display: 'flex', alignItems: 'center' }}>
                <span style={{ padding: '12px', background: 'rgba(0,0,0,0.4)', border: '1px solid var(--border-light)', borderRight: 'none', borderRadius: 'var(--radius-md) 0 0 var(--radius-md)', color: 'var(--text-muted)' }}>
                  scalelink.com/
                </span>
                <input 
                  type="text" 
                  className="form-input" 
                  style={{ borderRadius: '0 var(--radius-md) var(--radius-md) 0' }}
                  placeholder="my-custom-link" 
                  value={customAlias}
                  onChange={(e) => setCustomAlias(e.target.value)}
                />
              </div>
            </div>
            
            <button type="submit" className="btn btn-primary" style={{ width: '100%' }} disabled={isCreating}>
              {isCreating ? 'Shortening...' : <><Plus size={18} /> Create Short Link</>}
            </button>
          </form>
        </div>

        {/* User Stats Summary */}
        <div className="glass-panel" style={{ padding: '24px', display: 'flex', flexDirection: 'column', justifyContent: 'center' }}>
          <h3 style={{ marginBottom: '24px', color: 'var(--text-secondary)' }}>Lifetime Overview</h3>
          <div style={{ display: 'flex', gap: '40px' }}>
            <div>
              <p style={{ fontSize: '0.875rem', color: 'var(--text-muted)' }}>Total Links</p>
              <p className="text-gradient" style={{ fontSize: '3rem', fontWeight: '800' }}>{pageInfo.totalElements}</p>
            </div>
            <div>
              <p style={{ fontSize: '0.875rem', color: 'var(--text-muted)' }}>Total Clicks</p>
              <p style={{ fontSize: '3rem', fontWeight: '800' }}>
                {urls.reduce((sum, url) => sum + url.clickCount, 0)}
              </p>
            </div>
          </div>
        </div>
      </div>

      {/* URL List */}
      <h3 style={{ marginBottom: '20px' }}>Your Links</h3>
      
      {isLoading ? (
        <div className="loader-container"><div className="spinner"></div></div>
      ) : urls.length === 0 ? (
        <div className="glass-panel flex-center" style={{ padding: '60px', flexDirection: 'column', color: 'var(--text-muted)' }}>
          <Link2 size={48} style={{ marginBottom: '16px', opacity: 0.5 }} />
          <p>You haven't created any links yet.</p>
        </div>
      ) : (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
          {urls.map(url => (
            <div key={url.id} className="glass-panel" style={{ padding: '20px', display: 'flex', justifyContent: 'space-between', alignItems: 'center', transition: 'transform var(--transition-fast)' }}>
              
              <div style={{ overflow: 'hidden', paddingRight: '20px' }}>
                <a href={url.shortUrl} target="_blank" rel="noreferrer" style={{ fontSize: '1.25rem', fontWeight: '600', color: 'var(--accent-primary)', display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '8px' }}>
                  {url.shortUrl} <ExternalLink size={16} />
                </a>
                <p style={{ color: 'var(--text-secondary)', fontSize: '0.875rem', whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>
                  {url.originalUrl}
                </p>
                <p style={{ color: 'var(--text-muted)', fontSize: '0.75rem', marginTop: '8px' }}>
                  Created on {new Date(url.createdAt).toLocaleDateString()}
                </p>
              </div>

              <div style={{ display: 'flex', alignItems: 'center', gap: '20px' }}>
                <div style={{ textAlign: 'center', padding: '10px 20px', background: 'rgba(0,0,0,0.3)', borderRadius: 'var(--radius-md)' }}>
                  <BarChart2 size={16} color="var(--accent-tertiary)" style={{ marginBottom: '4px' }} />
                  <p style={{ fontWeight: '700', fontSize: '1.1rem' }}>{url.clickCount}</p>
                  <p style={{ fontSize: '0.7rem', color: 'var(--text-muted)', textTransform: 'uppercase' }}>Clicks</p>
                </div>
                
                <button 
                  onClick={() => copyToClipboard(url.shortUrl)}
                  className="btn btn-secondary" 
                  style={{ padding: '12px', borderRadius: 'var(--radius-md)' }}
                  title="Copy to clipboard"
                >
                  <Copy size={18} />
                </button>
              </div>
            </div>
          ))}

          {/* Pagination Controls */}
          {pageInfo.totalPages > 1 && (
            <div className="flex-center" style={{ gap: '16px', marginTop: '24px' }}>
              <button 
                className="btn btn-secondary" 
                disabled={pageInfo.pageNo === 0}
                onClick={() => fetchUrls(pageInfo.pageNo - 1)}
              >
                Previous
              </button>
              <span style={{ color: 'var(--text-secondary)' }}>
                Page {pageInfo.pageNo + 1} of {pageInfo.totalPages}
              </span>
              <button 
                className="btn btn-secondary" 
                disabled={pageInfo.pageNo >= pageInfo.totalPages - 1}
                onClick={() => fetchUrls(pageInfo.pageNo + 1)}
              >
                Next
              </button>
            </div>
          )}
        </div>
      )}
    </div>
  );
}
