// All content below is sourced from the NumberMerge 2048 game (com.hadify.NumberMerge2048).
export const PLAY_URL = 'https://play.google.com/store/apps/details?id=com.hadify.NumberMerge2048'

// Tile palette lifted straight from GameScreen.kt tileColors()
export const TILES = [
  { v: 2, bg: '#3f3a4a', fg: '#f1ebdf' },
  { v: 4, bg: '#4b4458', fg: '#f1ebdf' },
  { v: 8, bg: '#78543a', fg: '#ffffff' },
  { v: 16, bg: '#8f5135', fg: '#ffffff' },
  { v: 32, bg: '#a64b32', fg: '#ffffff' },
  { v: 64, bg: '#bf4331', fg: '#ffffff' },
  { v: 128, bg: '#ca8d38', fg: '#ffffff' },
  { v: 256, bg: '#d3a032', fg: '#ffffff' },
  { v: 512, bg: '#ddb42c', fg: '#ffffff' },
  { v: 1024, bg: '#e8c42a', fg: '#372a14' },
  { v: 2048, bg: '#f4d544', fg: '#372a14' },
]

export const SHOTS = [
  { src: '/assets/shots/01_home.png', label: 'Home', caption: 'A calm, dark, icon-first home built for one-tap play.' },
  { src: '/assets/shots/03_gameplay.png', label: 'Gameplay', caption: 'Swipe to slide every tile; matching numbers merge and double.' },
  { src: '/assets/shots/02_setup.png', label: 'New game setup', caption: 'Pick a 4×4, 5×5 or 6×6 board and Easy, Medium or Hard.' },
  { src: '/assets/shots/05_daily.png', label: 'Daily challenges', caption: 'Four tiers, multi-stage chains and bonus coins every day.' },
  { src: '/assets/shots/04_store.png', label: 'Power-up store', caption: 'Spend coins on Swap, Undo, Bomb and Freeze charges and packs.' },
  { src: '/assets/shots/06_howto.png', label: 'How to play', caption: 'Animated, visual tutorials whenever you need a refresher.' },
]

export const FEATURES = [
  { emoji: '🔀', title: 'Swap power-up', text: 'Trade the positions of any two tiles to set up a clean, game-saving merge.', accent: '#4e76ff' },
  { emoji: '↩️', title: 'Undo power-up', text: 'Reverse your last move and rescue the board from a mistake.', accent: '#7bc1ff' },
  { emoji: '💥', title: 'Bomb power-up', text: 'Detonate a tile to clear it and free up breathing room on a packed board.', accent: '#ff6d57' },
  { emoji: '❄️', title: 'Freeze power-up', text: 'Lock a tile in place for a few turns so it never moves when you swipe.', accent: '#73d7ff' },
  { emoji: '📐', title: '4×4, 5×5 & 6×6 boards', text: 'Choose the grid that fits your mood — from the classic 4×4 to a sprawling 6×6.', accent: '#ffa938' },
  { emoji: '🎚️', title: 'Easy · Medium · Hard', text: 'Difficulty tunes your starting power-ups, spawn rates, costs and coin rewards.', accent: '#63d883' },
]

export const STEPS = [
  { n: '01', title: 'Swipe to move', text: 'Swipe up, down, left or right to slide every tile across the board at once.' },
  { n: '02', title: 'Merge matching tiles', text: 'When two tiles with the same number collide they merge into one — 2 + 2 = 4.' },
  { n: '03', title: 'Reach 2048', text: 'Keep doubling 2 → 4 → 8 → … all the way to the legendary 2048 tile.' },
  { n: '04', title: 'Use power-ups & dailies', text: 'Deploy Swap, Undo, Bomb and Freeze when stuck, and clear daily challenge chains for coins.' },
]

export const STATS = [
  { value: 2048, suffix: '', label: 'Tile to chase' },
  { value: 4, suffix: '', label: 'Power-ups' },
  { value: 3, suffix: '', label: 'Board sizes' },
  { value: 100, suffix: '%', label: 'Free to play' },
]

// Difficulty details from CoreModels.kt tuningFor() / difficultyDescription()
export const DIFFICULTIES = [
  { name: 'Easy', accent: '#63d883', text: 'More starting power-ups, cheaper skills, and occasional bonus spawns.' },
  { name: 'Medium', accent: '#ffa938', text: 'The default balance, tuned for classic 2048 pacing.' },
  { name: 'Hard', accent: '#ff6d57', text: 'Harsher spawns, fewer assists, and higher power-up costs.' },
]

// Daily challenge tiers from CoreModels.kt ChallengeTier + DailyChallengeFactory.kt
export const CHALLENGES = [
  { tier: 'Beginner', coins: 70, board: '4×4', text: 'Short three-stage chains mixing score and tile goals to learn the rhythm.' },
  { tier: 'Intermediate', coins: 130, board: '4×4', text: 'Tighter chains with bomb discipline and move limits.' },
  { tier: 'Advanced', coins: 210, board: '5×5', text: 'Bigger boards demanding score rhythm and 1024-tile scaling.' },
  { tier: 'Expert', coins: 320, board: '6×6', text: 'No-power-up runs and 2048 apex routes under a strict move cap.' },
]
