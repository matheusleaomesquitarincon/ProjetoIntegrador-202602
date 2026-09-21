import { StrictMode, useEffect, useState } from 'react';
import { createRoot } from 'react-dom/client';
import { marked } from 'marked';
import { AuthProvider, useAuth } from './context/AuthContext';
import PrivateRoute from './components/PrivateRoute';
import Login from './pages/Login';
import Cadastro from './pages/Cadastro';
import './styles.css';

const tabs = [
  { id: 'inicio', label: 'Início', icon: '⌂' },
  { id: 'anotacoes', label: 'Anotações', icon: '▤' },
  { id: 'materiais', label: 'Materiais', icon: '▥' },
  { id: 'quiz', label: 'Quiz', icon: '?' },
];

function App() {
  const { user, loading, logout } = useAuth();
  const [activeTab, setActiveTab] = useState('inicio');
  const [darkMode, setDarkMode] = useState(false);
  const [authView, setAuthView] = useState('login');

  if (loading) return <div className="auth-loading">Verificando sua sessão...</div>;
  function navigate(target) {
    if (!user && ['anotacoes', 'quiz', 'usuario'].includes(target)) {
      setAuthView('login');
      setActiveTab(target);
      return;
    }
    setActiveTab(target);
  }
  if (!user && ['anotacoes', 'quiz', 'usuario'].includes(activeTab)) return authView === 'login' ? <Login onCreateAccount={() => setAuthView('cadastro')} /> : <Cadastro onBackToLogin={() => setAuthView('login')} />;

  return (
    <div className={darkMode ? 'app dark' : 'app'}>
      <header className="topbar">
        <a className="brand" href="#inicio" onClick={() => navigate('inicio')}>
          <span className="brand-mark">J</span>
          <span>Java<span className="brand-accent">Studio</span></span>
        </a>

        <nav className="navigation" aria-label="Navegação principal">
          {tabs.map((tab) => (
            <button
              className={activeTab === tab.id ? 'nav-item active' : 'nav-item'}
              key={tab.id}
              onClick={() => navigate(tab.id)}
            >
              <span className="nav-icon" aria-hidden="true">{tab.icon}</span>
              {tab.label}
            </button>
          ))}
        </nav>

        <div className="header-actions">
          <button className="theme-toggle" onClick={() => setDarkMode(!darkMode)} aria-label="Alternar modo claro e escuro">
            <span aria-hidden="true">{darkMode ? '☼' : '☾'}</span>
            <span className="theme-label">{darkMode ? 'Modo claro' : 'Modo escuro'}</span>
          </button>
          <button className="profile-button" onClick={() => navigate('usuario')} aria-label={user ? 'Abrir perfil do usuário' : 'Abrir tela de login'}>
            <span className="avatar">{user ? user.email.charAt(0).toUpperCase() : '↗'}</span>
            <span className="profile-name">{user ? 'Meu perfil' : 'Entrar'}</span>
            <span className="chevron" aria-hidden="true">⌄</span>
          </button>
        </div>
      </header>

      <main>
        {activeTab === 'inicio' && <Home onNavigate={navigate} />}
        {activeTab === 'materiais' && <Materials />}
        {user && <PrivateRoute>
          {activeTab === 'anotacoes' && <Notes />}
          {activeTab === 'quiz' && <Quiz />}
          {activeTab === 'usuario' && <UserSettings darkMode={darkMode} setDarkMode={setDarkMode} user={user} onLogout={logout} />}
        </PrivateRoute>}
      </main>
    </div>
  );
}

function Home({ onNavigate }) {
  return (
    <div className="page home-page">
      <section className="hero-section">
        <div className="hero-copy">
          <p className="eyebrow">Seu espaço de aprendizado</p>
          <h1>Aprenda Java.<br /><span>Construa possibilidades.</span></h1>
          <p className="hero-text">Um lugar simples para organizar seus estudos, testar seus conhecimentos e evoluir no seu ritmo.</p>
          <button className="primary-button" onClick={() => onNavigate('anotacoes')}>Começar a estudar <span aria-hidden="true">→</span></button>
        </div>
        <div className="code-art" aria-label="Ilustração decorativa de código Java">
          <div className="code-window">
            <div className="window-dots"><i></i><i></i><i></i><span>Welcome.java</span></div>
            <pre><code><em>public class</em> <strong>Welcome</strong> {'{'}{`\n`}  <em>public static void</em> main(String[] args) {'{'}{`\n`}    System.out.println(<b>"Olá, Java!"</b>);{`\n`}  {'}'}{`\n`}{'}'}</code></pre>
          </div>
          <span className="art-chip chip-one">{`{ }`}</span>
          <span className="art-chip chip-two">&lt;/&gt;</span>
        </div>
      </section>

      <section className="content-section">
        <div className="section-heading">
          <div><p className="eyebrow">Ponto de partida</p><h2>O que é Java?</h2></div>
          <span className="section-number">01</span>
        </div>
        <article className="java-content">
          <h3>O Motor por Trás das Grandes Aplicações</h3>
          <p>Criada em 1995 pela Sun Microsystems (e hoje mantida pela Oracle), o <strong>Java</strong> é uma das linguagens de programação mais populares, influentes e requisitadas do mundo. Ela é uma linguagem de alto nível, fortemente tipada e puramente baseada no paradigma de <strong>Orientação a Objetos</strong>.</p>
          <p>Seja no sistema do seu banco, em aplicativos de celular ou na infraestrutura de grandes gigantes da tecnologia, o Java provavelmente está lá, operando nos bastidores.</p>

          <h3>A Magia da JVM: <em>Escreva uma vez, rode em qualquer lugar</em></h3>
          <p>O grande diferencial que popularizou o Java logo em seu lançamento foi o conceito de <em>WORA</em> (<em>Write Once, Run Anywhere</em>). Diferente de linguagens como C ou C++, onde o código precisa ser compilado especificamente para o sistema operacional, o código Java passa por um processo diferente:</p>
          <ol>
            <li>Você escreve o código-fonte (<code>.java</code>).</li>
            <li>O compilador traduz esse código para um formato intermediário chamado <strong>Bytecode</strong> (<code>.class</code>).</li>
            <li>A <strong>JVM (Java Virtual Machine)</strong> lê esse Bytecode e o executa no sistema operacional onde ela está instalada.</li>
          </ol>
          <p>Isso significa que você pode programar um sistema no seu computador com Windows e rodá-lo em um servidor Linux na nuvem sem precisar alterar uma única linha de código.</p>

          <h3>Por que o Java continua tão forte?</h3>
          <p>Apesar de ser uma linguagem veterana, o Java se moderniza constantemente. Algumas das características que o mantêm no topo do mercado incluem:</p>
          <ul>
            <li><strong>Orientação a Objetos:</strong> Tudo no Java gira em torno de objetos e classes. Isso facilita a organização do código, o reaproveitamento de rotinas e a manutenção de sistemas complexos.</li>
            <li><strong>Robustez e Segurança:</strong> O Java possui um gerenciamento automático de memória (o <em>Garbage Collector</em>), que limpa dados que não estão mais sendo usados, evitando travamentos. Além disso, foi projetado com fortes barreiras de segurança para ambientes corporativos.</li>
            <li><strong>Ecossistema Gigante:</strong> A comunidade Java é uma das maiores do mundo. Existem bibliotecas e frameworks maduros para absolutamente tudo. O <strong>Spring Boot</strong>, por exemplo, é o padrão absoluto da indústria para a criação de APIs e serviços de backend rápidos e escaláveis.</li>
            <li><strong>Multithreading Nativo:</strong> O Java lida excepcionalmente bem com a execução de múltiplas tarefas ao mesmo tempo, tornando-o ideal para aplicações que recebem milhares de acessos simultâneos.</li>
          </ul>

          <h3>Onde o Java é usado no mundo real?</h3>
          <p>Se você decidir aprender Java, encontrará oportunidades em diversas áreas da tecnologia:</p>
          <ul>
            <li><strong>Desenvolvimento Backend Corporativo:</strong> Bancos, seguradoras e empresas de e-commerce confiam no Java para processar transações financeiras e gerenciar bancos de dados pesados.</li>
            <li><strong>Sistemas Distribuídos e Mensageria:</strong> O Java é frequentemente utilizado em arquiteturas de microsserviços, integrando-se perfeitamente com ferramentas de mensageria assíncrona e contêineres.</li>
            <li><strong>Aplicativos Android:</strong> Por muitos anos, o Java foi a linguagem oficial e exclusiva para a criação de aplicativos nativos para o sistema Android.</li>
            <li><strong>Big Data:</strong> Ferramentas gigantes de processamento de dados, como o Apache Hadoop e o Apache Kafka, são escritas em Java (ou em linguagens baseadas na JVM).</li>
          </ul>
          <p>Aprender Java não é apenas aprender a sintaxe de uma linguagem; é compreender os fundamentos da engenharia de software moderna. Ao dominar seus conceitos, você constrói uma base sólida que facilita o aprendizado de qualquer outra tecnologia no futuro, além de abrir portas para um mercado de trabalho com alta demanda por profissionais qualificados.</p>
        </article>
      </section>

      <section className="path-section">
        <div className="section-heading"><div><p className="eyebrow">Continue no seu ritmo</p><h2>Monte seu caminho</h2></div></div>
        <div className="path-grid">
          <PathCard number="01" title="Faça anotações" text="Guarde conceitos, exemplos e ideias importantes." action="Abrir anotações" onClick={() => onNavigate('anotacoes')} />
          <PathCard number="02" title="Explore materiais" text="Escolha um tópico e encontre conteúdos para estudar." action="Ver materiais" onClick={() => onNavigate('materiais')} />
          <PathCard number="03" title="Teste seus conhecimentos" text="Responda perguntas e acompanhe sua evolução." action="Ir para o quiz" onClick={() => onNavigate('quiz')} />
        </div>
      </section>
    </div>
  );
}

function PathCard({ number, title, text, action, onClick }) {
  return <article className="path-card"><span className="card-number">{number}</span><h3>{title}</h3><p>{text}</p><button className="text-button" onClick={onClick}>{action} <span>→</span></button></article>;
}

function Notes() {
  const [notes, setNotes] = useState([]);
  const [selectedId, setSelectedId] = useState(null);
  const [title, setTitle] = useState('');
  const [content, setContent] = useState('');
  const [feedback, setFeedback] = useState('');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetch('http://localhost:8080/api/notas', { credentials: 'include' })
      .then((response) => {
        if (!response.ok) throw new Error('Não foi possível carregar as anotações.');
        return response.json();
      })
      .then(setNotes)
      .catch(() => setFeedback('Não foi possível conectar ao backend Java.'))
      .finally(() => setLoading(false));
  }, []);

  function startNewNote() {
    setSelectedId(null);
    setTitle('');
    setContent('');
    setFeedback('');
  }

  function selectNote(note) {
    setSelectedId(note.id);
    setTitle(note.titulo);
    setContent(note.conteudo);
    setFeedback('');
  }

  async function saveNote() {
    if (!title.trim() || !content.trim()) {
      setFeedback('Preencha o título e o conteúdo antes de salvar.');
      return;
    }
    const method = selectedId ? 'PUT' : 'POST';
    const endpoint = selectedId ? `http://localhost:8080/api/notas/${selectedId}` : 'http://localhost:8080/api/notas';
    const response = await fetch(endpoint, { method, credentials: 'include', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ titulo: title.trim(), conteudo: content.trim() }) });
    if (!response.ok) { setFeedback('Não foi possível salvar a anotação.'); return; }
    const savedNote = await response.json();
    setNotes((currentNotes) => selectedId ? currentNotes.map((note) => note.id === selectedId ? savedNote : note) : [savedNote, ...currentNotes]);
    setSelectedId(savedNote.id);
    setFeedback('Anotação salva no backend.');
  }

  async function deleteNote() {
    if (!selectedId || !window.confirm('Apagar esta anotação?')) return;
    const response = await fetch(`http://localhost:8080/api/notas/${selectedId}`, { method: 'DELETE', credentials: 'include' });
    if (!response.ok) { setFeedback('Não foi possível apagar a anotação.'); return; }
    setNotes((currentNotes) => currentNotes.filter((note) => note.id !== selectedId));
    startNewNote();
  }

  return <div className="page inner-page"><PageIntro eyebrow="Seu caderno" title="Anotações" text="Organize o que você está aprendendo em um só lugar." /><section className="notes-layout"><aside className="notes-sidebar"><button className="primary-button full-width" onClick={startNewNote}><span>+</span> Nova anotação</button><div className="note-list">{loading ? <div className="note-list-empty"><p>Carregando anotações...</p></div> : notes.length === 0 ? <div className="note-list-empty"><span>▤</span><p>Suas anotações<br />aparecerão aqui.</p></div> : notes.map((note) => <button className={selectedId === note.id ? 'note-item selected' : 'note-item'} key={note.id} onClick={() => selectNote(note)}><strong>{note.titulo}</strong><span>{note.conteudo.slice(0, 42)}{note.conteudo.length > 42 ? '...' : ''}</span></button>)}</div></aside><div className="note-editor"><input value={title} onChange={(event) => setTitle(event.target.value)} placeholder="Título da anotação" aria-label="Título da anotação" /><textarea value={content} onChange={(event) => setContent(event.target.value)} placeholder="Escreva aqui suas ideias sobre Java..." aria-label="Conteúdo da anotação" /><div className="editor-footer"><span className={feedback.includes('salva') ? 'success-message' : 'error-message'}>{feedback || 'Alterações no backend'}</span><div className="note-actions">{selectedId && <button className="delete-button" onClick={deleteNote}>Apagar</button>}<button className="save-button" onClick={saveNote}>Salvar anotação</button></div></div></div></section></div>;
}

function Materials() {
  const [topic, setTopic] = useState('fundamentos');
  const [difficulty, setDifficulty] = useState('iniciante');
  const [material, setMaterial] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    setLoading(true);
    setError('');
    fetch(`http://localhost:8080/api/materiais/${topic}/${difficulty}`, { credentials: 'include' })
      .then((response) => {
        if (!response.ok) throw new Error('Material não encontrado.');
        return response.json();
      })
      .then(setMaterial)
      .catch((requestError) => setError(requestError.message))
      .finally(() => setLoading(false));
  }, [topic, difficulty]);

  async function downloadMarkdown() {
    const response = await fetch(`http://localhost:8080/api/materiais/${topic}/${difficulty}/md`, { credentials: 'include' });
    if (!response.ok) { setError('Não foi possível baixar o material.'); return; }
    const file = await response.blob();
    const link = document.createElement('a');
    link.href = URL.createObjectURL(file);
    link.download = `${topic}-${difficulty}.md`;
    link.click();
    URL.revokeObjectURL(link.href);
  }

  return <div className="page inner-page"><PageIntro eyebrow="Aprenda por tópicos" title="Materiais" text="Escolha um assunto e um nível para encontrar o material teórico ideal para você." /><section className="materials-panel"><div className="material-fields"><label>Tópico da linguagem Java<select value={topic} onChange={(event) => setTopic(event.target.value)}><option value="fundamentos">Fundamentos</option><option value="orientacao-a-objetos">Orientação a Objetos</option><option value="collections">Collections</option><option value="excecoes">Exceções</option><option value="spring">Spring Boot</option></select></label><label>Dificuldade<select value={difficulty} onChange={(event) => setDifficulty(event.target.value)}><option value="iniciante">Iniciante</option><option value="intermediario">Intermediário</option><option value="avancado">Avançado</option></select></label></div>{loading && <div className="material-placeholder"><p>Carregando material...</p></div>}{error && <div className="material-placeholder"><p>{error}</p></div>}{material && !loading && <article className="material-reader"><div className="material-reader-header"><span className="card-number">{material.dificuldade}</span><button className="secondary-button" onClick={downloadMarkdown}>Baixar Markdown</button></div><div className="markdown-content" dangerouslySetInnerHTML={{ __html: marked.parse(material.conteudo) }} /></article>}</section></div>;
}

function Quiz() {
  return <div className="page inner-page"><PageIntro eyebrow="Pratique" title="Quiz de Java" text="Teste o que você aprendeu e descubra onde pode continuar evoluindo." /><div className="quiz-empty"><div className="quiz-symbol">?</div><h2>Seu primeiro quiz está sendo preparado</h2><p>As perguntas teóricas sobre Java serão adicionadas aqui.</p><button className="secondary-button" disabled>Em breve</button></div></div>;
}

function UserSettings({ darkMode, setDarkMode, user, onLogout }) {
  return <div className="page inner-page"><PageIntro eyebrow="Personalize sua experiência" title="Meu perfil" text="Ajuste suas preferências para estudar do seu jeito." /><section className="settings-panel"><div className="setting-profile"><span className="large-avatar">{user.email.charAt(0).toUpperCase()}</span><div><h2>{user.email}</h2><p>Conta do Java Studio</p></div><button className="secondary-button" onClick={onLogout}>Sair da conta</button></div><div className="setting-row"><div><h3>Aparência</h3><p>Escolha como o Java Studio aparece para você.</p></div><label className="switch-label"><span>{darkMode ? 'Modo escuro' : 'Modo claro'}</span><button className={darkMode ? 'switch on' : 'switch'} onClick={() => setDarkMode(!darkMode)} aria-label="Alternar tema"><span /></button></label></div></section></div>;
}

function PageIntro({ eyebrow, title, text }) {
  return <div className="page-intro"><p className="eyebrow">{eyebrow}</p><h1>{title}</h1><p>{text}</p></div>;
}

createRoot(document.getElementById('root')).render(<StrictMode><AuthProvider><App /></AuthProvider></StrictMode>);