import { GraphicEntityModule } from './entity-module/GraphicEntityModule.js';
import { TooltipModule } from './tooltip-module/TooltipModule.js';
import { EndScreenModule } from './endscreen-module/EndScreenModule.js';
import { ToggleModule } from './toggle-module/ToggleModule.js';

export const modules = [
    GraphicEntityModule,
    TooltipModule,
    EndScreenModule,
    ToggleModule
];

export const playerColors = [
    '#ff1d5c', // Player 0: Crimson Red
    '#22a1e4'  // Player 1: Cyber Blue
];

export const gameName = 'ChronoTactics';

export const options = [
    ToggleModule.defineToggle({
        toggle: 'debugOverlay',
        title: 'SHOW UNIT IDS',
        values: {
            'ON': true,
            'OFF': false
        },
        default: true
    })
];
