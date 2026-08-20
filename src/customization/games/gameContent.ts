/**
 * Game content definitions (Phase 11).
 *
 * All question banks for couple mini-games. Owner-editable:
 * change questions, add more, adjust categories — game engine stays unchanged.
 *
 * Each game definition includes its question bank with the game rules,
 * scoring type, and presentation metadata.
 */

import type { GameDefinition, GameQuestion, GameType } from '../../data/game/gameTypes.ts';

// ---------------------------------------------------------------------------
// Who Knows Who Better — player2 guesses player1's answers
// ---------------------------------------------------------------------------

const whoKnowsWhoBetterQuestions: GameQuestion[] = [
  { id: 'wkhb-1', text: 'What is my favorite food?', correctAnswer: 'Pizza', category: 'favorites' },
  { id: 'wkhb-2', text: 'What is my go-to comfort movie?', correctAnswer: 'The Notebook', category: 'favorites' },
  { id: 'wkhb-3', text: 'What song always makes me dance?', correctAnswer: 'September', category: 'favorites' },
  { id: 'wkhb-4', text: 'What is my biggest pet peeve?', correctAnswer: 'Loud chewing', category: 'personality' },
  { id: 'wkhb-5', text: 'What is my dream vacation destination?', correctAnswer: 'Paris', category: 'dreams' },
  { id: 'wkhb-6', text: 'What would I bring to a desert island?', correctAnswer: 'Phone', category: 'hypothetical' },
  { id: 'wkhb-7', text: 'What is my most used emoji?', correctAnswer: 'Heart', category: 'personality' },
  { id: 'wkhb-8', text: 'What time do I usually wake up?', correctAnswer: '7:00 AM', category: 'daily' },
  { id: 'wkhb-9', text: 'What is my hidden talent?', correctAnswer: 'Singing', category: 'personality' },
  { id: 'wkhb-10', text: 'What makes me laugh the most?', correctAnswer: 'Puns', category: 'personality' },
  { id: 'wkhb-11', text: 'What is my favorite season?', correctAnswer: 'Fall', category: 'favorites' },
  { id: 'wkhb-12', text: 'What is my go-to coffee order?', correctAnswer: 'Latte', category: 'daily' },
  { id: 'wkhb-13', text: 'What do I do when Im stressed?', correctAnswer: 'Take a walk', category: 'personality' },
  { id: 'wkhb-14', text: 'What is my love language?', correctAnswer: 'Words of affirmation', category: 'relationship' },
  { id: 'wkhb-15', text: 'What is my favorite thing about us?', correctAnswer: 'How we laugh together', category: 'relationship' },
];

export const WHO_KNOWS_WHO_BETTER: GameDefinition = {
  type: 'who-knows-who-better',
  title: 'Who Knows Who Better?',
  description: 'Player 1 answers questions about themselves, then Player 2 guesses what Player 1 said.',
  questionsPerRound: 10,
  turnBased: true,
  scoringType: 'match',
  questions: whoKnowsWhoBetterQuestions,
};

// ---------------------------------------------------------------------------
// Guess My Answer — similar to Who Knows but reversed roles
// ---------------------------------------------------------------------------

const guessMyAnswerQuestions: GameQuestion[] = [
  { id: 'gma-1', text: 'What is the first thing you notice about me?', correctAnswer: 'My smile', category: 'first impressions' },
  { id: 'gma-2', text: 'What is my favorite thing we do together?', correctAnswer: 'Cooking', category: 'activities' },
  { id: 'gma-3', text: 'Where was our best date?', correctAnswer: 'The beach', category: 'memories' },
  { id: 'gma-4', text: 'What song reminds you of me?', correctAnswer: 'Our song', category: 'favorites' },
  { id: 'gma-5', text: 'What is my best quality?', correctAnswer: 'Kindness', category: 'personality' },
  { id: 'gma-6', text: 'What gift would make me happiest?', correctAnswer: 'A handwritten letter', category: 'favorites' },
  { id: 'gma-7', text: 'What is our couple superpower?', correctAnswer: 'Communication', category: 'relationship' },
  { id: 'gma-8', text: 'What is my biggest dream?', correctAnswer: 'Travel the world', category: 'dreams' },
  { id: 'gma-9', text: 'What makes me feel loved?', correctAnswer: 'Quality time', category: 'love languages' },
  { id: 'gma-10', text: 'What is my favorite memory of us?', correctAnswer: 'First trip together', category: 'memories' },
  { id: 'gma-11', text: 'What would I order at a restaurant?', correctAnswer: 'Pasta', category: 'daily' },
  { id: 'gma-12', text: 'What am I most grateful for?', correctAnswer: 'You', category: 'relationship' },
];

export const GUESS_MY_ANSWER: GameDefinition = {
  type: 'guess-my-answer',
  title: 'Guess My Answer',
  description: 'One partner answers questions about the other. How well do you really know each other?',
  questionsPerRound: 10,
  turnBased: true,
  scoringType: 'match',
  questions: guessMyAnswerQuestions,
};

// ---------------------------------------------------------------------------
// Would You Rather — opinion-based choices
// ---------------------------------------------------------------------------

const wouldYouRatherQuestions: GameQuestion[] = [
  { id: 'wyr-1', text: 'Would you rather stay in or go out for date night?', options: ['Stay in', 'Go out'] },
  { id: 'wyr-2', text: 'Would you rather have a beach vacation or a mountain retreat?', options: ['Beach', 'Mountains'] },
  { id: 'wyr-3', text: 'Would you rather cook together or order takeout?', options: ['Cook together', 'Order takeout'] },
  { id: 'wyr-4', text: 'Would you rather have a pet dog or a pet cat?', options: ['Dog', 'Cat'] },
  { id: 'wyr-5', text: 'Would you rather watch a comedy or a drama?', options: ['Comedy', 'Drama'] },
  { id: 'wyr-6', text: 'Would you rather have a slow morning or a productive morning?', options: ['Slow morning', 'Productive morning'] },
  { id: 'wyr-7', text: 'Would you rather plan every detail or be spontaneous?', options: ['Plan everything', 'Be spontaneous'] },
  { id: 'wyr-8', text: 'Would you rather have a big wedding or a small intimate one?', options: ['Big wedding', 'Small & intimate'] },
  { id: 'wyr-9', text: 'Would you rather travel to the past or the future?', options: ['Past', 'Future'] },
  { id: 'wyr-10', text: 'Would you rather never cook or never do dishes?', options: ['Never cook', 'Never do dishes'] },
  { id: 'wyr-11', text: 'Would you rather have a raise or more vacation days?', options: ['Raise', 'More vacation'] },
  { id: 'wyr-12', text: 'Would you rather live in the city or the countryside?', options: ['City', 'Countryside'] },
];

export const WOULD_YOU_RATHER: GameDefinition = {
  type: 'would-you-rather',
  title: 'Would You Rather?',
  description: 'Both players choose their preference. See how often you agree!',
  questionsPerRound: 10,
  turnBased: false,
  scoringType: 'choice',
  questions: wouldYouRatherQuestions,
};

// ---------------------------------------------------------------------------
// Couple Trivia — knowledge about each other
// ---------------------------------------------------------------------------

const coupleTriviaQuestions: GameQuestion[] = [
  { id: 'ct-1', text: 'What year did you first meet?', options: ['2020', '2021', '2022', '2023'] },
  { id: 'ct-2', text: 'Where was your first date?', options: ['Restaurant', 'Coffee shop', 'Park', 'Movie theater'] },
  { id: 'ct-3', text: 'Who said "I love you" first?', options: ['Partner 1', 'Partner 2', "Can't remember"] },
  { id: 'ct-4', text: 'What is your anniversary?', options: ['January', 'March', 'June', 'September'] },
  { id: 'ct-5', text: 'Who is the better cook?', options: ['Partner 1', 'Partner 2', 'We both are'] },
  { id: 'ct-6', text: 'Who takes longer to get ready?', options: ['Partner 1', 'Partner 2', 'Same time'] },
  { id: 'ct-7', text: 'Who is more romantic?', options: ['Partner 1', 'Partner 2', 'Equally'] },
  { id: 'ct-8', text: 'Who is the morning person?', options: ['Partner 1', 'Partner 2', 'Neither'] },
  { id: 'ct-9', text: 'Who is more likely to plan a surprise?', options: ['Partner 1', 'Partner 2', 'We both do'] },
  { id: 'ct-10', text: 'Who falls asleep first?', options: ['Partner 1', 'Partner 2', 'At the same time'] },
];

export const COUPLE_TRIVIA: GameDefinition = {
  type: 'couple-trivia',
  title: 'Couple Trivia',
  description: 'Test your knowledge about each other with trivia questions!',
  questionsPerRound: 10,
  turnBased: false,
  scoringType: 'match',
  questions: coupleTriviaQuestions,
};

// ---------------------------------------------------------------------------
// This or That — quick preference choices
// ---------------------------------------------------------------------------

const thisOrThatQuestions: GameQuestion[] = [
  { id: 'tot-1', text: 'Coffee or Tea?', options: ['Coffee', 'Tea'] },
  { id: 'tot-2', text: 'Morning or Night?', options: ['Morning', 'Night'] },
  { id: 'tot-3', text: 'Text or Call?', options: ['Text', 'Call'] },
  { id: 'tot-4', text: 'Sweet or Savory?', options: ['Sweet', 'Savory'] },
  { id: 'tot-5', text: 'Indoor or Outdoor?', options: ['Indoor', 'Outdoor'] },
  { id: 'tot-6', text: 'Books or Movies?', options: ['Books', 'Movies'] },
  { id: 'tot-7', text: 'Plan or Spontaneous?', options: ['Plan', 'Spontaneous'] },
  { id: 'tot-8', text: 'City or Beach?', options: ['City', 'Beach'] },
  { id: 'tot-9', text: 'Call or Text?', options: ['Call', 'Text'] },
  { id: 'tot-10', text: 'Cook at home or Dine out?', options: ['Cook at home', 'Dine out'] },
  { id: 'tot-11', text: 'Pineapple on pizza: Yes or No?', options: ['Yes', 'No'] },
  { id: 'tot-12', text: 'Blanket or Fan?', options: ['Blanket', 'Fan'] },
];

export const THIS_OR_THAT: GameDefinition = {
  type: 'this-or-that',
  title: 'This or That',
  description: 'Quick-fire preferences — pick one!',
  questionsPerRound: 10,
  turnBased: false,
  scoringType: 'choice',
  questions: thisOrThatQuestions,
};

// ---------------------------------------------------------------------------
// Finish My Sentence — creative fill-in-the-blank
// ---------------------------------------------------------------------------

const finishMySentenceQuestions: GameQuestion[] = [
  { id: 'fms-1', text: 'Our first date was...', correctAnswer: 'Amazing' },
  { id: 'fms-2', text: 'The best thing about you is...', correctAnswer: 'Your kindness' },
  { id: 'fms-3', text: 'I knew it was love when...', correctAnswer: 'When we first talked' },
  { id: 'fms-4', text: 'Our dream home would be...', correctAnswer: 'By the ocean' },
  { id: 'fms-5', text: 'If we could travel anywhere...', correctAnswer: 'Italy' },
  { id: 'fms-6', text: 'My favorite thing about us is...', correctAnswer: 'How we laugh' },
  { id: 'fms-7', text: 'You always make me feel...', correctAnswer: 'Loved' },
  { id: 'fms-8', text: 'Our next adventure should be...', correctAnswer: 'A road trip' },
  { id: 'fms-9', text: 'I appreciate you most when...', correctAnswer: 'You listen' },
  { id: 'fms-10', text: 'In 10 years, we will be...', correctAnswer: 'Still in love' },
];

export const FINISH_MY_SENTENCE: GameDefinition = {
  type: 'finish-my-sentence',
  title: 'Finish My Sentence',
  description: 'Complete the sentence and see how your partner finishes it!',
  questionsPerRound: 10,
  turnBased: true,
  scoringType: 'match',
  questions: finishMySentenceQuestions,
};

// ---------------------------------------------------------------------------
// All games registry
// ---------------------------------------------------------------------------

export const ALL_GAME_DEFINITIONS: GameDefinition[] = [
  WHO_KNOWS_WHO_BETTER,
  GUESS_MY_ANSWER,
  WOULD_YOU_RATHER,
  COUPLE_TRIVIA,
  THIS_OR_THAT,
  FINISH_MY_SENTENCE,
];

export function getGameDefinition(type: GameType): GameDefinition | undefined {
  return ALL_GAME_DEFINITIONS.find((g) => g.type === type);
}


