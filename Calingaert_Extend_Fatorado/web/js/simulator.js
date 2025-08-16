class VirtualMachine {
    constructor() {
        this.memory = new Array(1024).fill(0); // 1KB mínimo
        this.registers = {
            PC: 0,    // Program Counter
            SP: 0,    // Stack Pointer
            ACC: 0,   // Accumulator
            MOP: 2,   // Modo de Operação
            RI: 0,    // Registrador de Instrução
            RE: 0,    // Registrador de Endereço
            R0: 0,    // Registrador Geral 0
            R1: 0     // Registrador Geral 1
        };
        
        this.inputQueue = [];
        this.outputBuffer = [];
        this.isRunning = false;
        this.isPaused = false;
        this.cycleCount = 0;
        this.currentInstruction = null;
        this.stackLimit = 10;
        
        this.opcodes = {
            0: 'BR', 1: 'BRPOS', 2: 'ADD', 3: 'LOAD', 4: 'BRZERO',
            5: 'BRNEG', 6: 'SUB', 7: 'STORE', 8: 'WRITE', 10: 'DIVIDE',
            11: 'STOP', 12: 'READ', 13: 'COPY', 14: 'MULT', 15: 'CALL',
            16: 'RET', 17: 'PUSH', 18: 'POP'
        };
        
        this.initializeUI();
    }

    initializeUI() {
        this.updateMemoryDisplay();
        this.updateRegistersDisplay();
        this.updateStatus();
    }

    loadProgram(binaryData) {
        try {
            let address = 0;
            for (let i = 0; i < binaryData.length; i += 16) {
                const wordStr = binaryData.substr(i, 16).padEnd(16, '0');
                const value = parseInt(wordStr, 2);
                if (!isNaN(value) && address < this.memory.length) {
                    this.memory[address] = value;
                    address++;
                }
            }
            
            this.showMessage(`Programa carregado com sucesso! ${address} palavras carregadas.`, 'success');
            this.updateMemoryDisplay();
            return true;
        } catch (error) {
            this.showMessage(`Erro ao carregar programa: ${error.message}`, 'error');
            return false;
        }
    }

    executeInstruction() {
        if (this.registers.PC >= this.memory.length) {
            this.showMessage('Erro: PC fora dos limites da memória', 'error');
            this.stop();
            return false;
        }

        const instruction = this.memory[this.registers.PC];
        this.registers.RI = instruction;
        
        const immediateMode = (instruction & 128) !== 0;
        const indirect1 = (instruction & 32) !== 0;
        const indirect2 = (instruction & 64) !== 0;
        const baseOpcode = instruction & 31;
        
        const opcodeStr = this.opcodes[baseOpcode] || 'UNKNOWN';
        
        this.currentInstruction = {
            address: this.registers.PC,
            opcode: baseOpcode,
            instruction: instruction,
            mnemonic: opcodeStr,
            immediate: immediateMode,
            indirect1: indirect1,
            indirect2: indirect2
        };

        this.registers.PC++;
        
        try {
            switch (baseOpcode) {
                case 0: this.executeBR(immediateMode, indirect1); break;
                case 1: this.executeBRPOS(immediateMode, indirect1); break;
                case 2: this.executeADD(immediateMode, indirect1); break;
                case 3: this.executeLOAD(immediateMode, indirect1); break;
                case 4: this.executeBRZERO(immediateMode, indirect1); break;
                case 5: this.executeBRNEG(immediateMode, indirect1); break;
                case 6: this.executeSUB(immediateMode, indirect1); break;
                case 7: this.executeSTORE(immediateMode, indirect1); break;
                case 8: this.executeWRITE(immediateMode, indirect1); break;
                case 10: this.executeDIVIDE(immediateMode, indirect1); break;
                case 11: this.executeSTOP(); return false;
                case 12: this.executeREAD(immediateMode, indirect1); break;
                case 13: this.executeCOPY(immediateMode, indirect1, indirect2); break;
                case 14: this.executeMULT(immediateMode, indirect1); break;
                case 15: this.executeCALL(immediateMode, indirect1); break;
                case 16: this.executeRET(); break;
                case 17: this.executePUSH(); break;
                case 18: this.executePOP(); break;
                default:
                    this.showMessage(`Instrução desconhecida: ${baseOpcode}`, 'error');
                    this.stop();
                    return false;
            }
        } catch (error) {
            this.showMessage(`Erro na execução: ${error.message}`, 'error');
            this.stop();
            return false;
        }

        this.cycleCount++;
        return true;
    }

    getOperand(immediate, indirect) {
        if (this.registers.PC >= this.memory.length) {
            throw new Error('PC fora dos limites para operando');
        }
        
        let operand = this.memory[this.registers.PC];
        this.registers.PC++;
        
        if (immediate) {
            return operand;
        } else if (indirect) {
            if (operand >= this.memory.length) {
                throw new Error('Endereço indireto fora dos limites');
            }
            return this.memory[operand];
        } else {
            return operand;
        }
    }

    getOperandAddress(immediate, indirect) {
        if (this.registers.PC >= this.memory.length) {
            throw new Error('PC fora dos limites para endereço');
        }
        
        let address = this.memory[this.registers.PC];
        this.registers.PC++;
        
        if (immediate) {
            throw new Error('Modo imediato não válido para endereço');
        } else if (indirect) {
            if (address >= this.memory.length) {
                throw new Error('Endereço indireto fora dos limites');
            }
            return this.memory[address];
        } else {
            return address;
        }
    }

    executeBR(immediate, indirect) {
        const address = this.getOperandAddress(immediate, indirect);
        this.registers.PC = address;
    }

    executeBRPOS(immediate, indirect) {
        const address = this.getOperandAddress(immediate, indirect);
        if (this.registers.ACC > 0) {
            this.registers.PC = address;
        }
    }

    executeBRZERO(immediate, indirect) {
        const address = this.getOperandAddress(immediate, indirect);
        if (this.registers.ACC === 0) {
            this.registers.PC = address;
        }
    }

    executeBRNEG(immediate, indirect) {
        const address = this.getOperandAddress(immediate, indirect);
        if (this.registers.ACC < 0) {
            this.registers.PC = address;
        }
    }

    executeADD(immediate, indirect) {
        const value = this.getOperand(immediate, indirect);
        this.registers.ACC = (this.registers.ACC + value) & 0xFFFF;
    }

    executeSUB(immediate, indirect) {
        const value = this.getOperand(immediate, indirect);
        this.registers.ACC = (this.registers.ACC - value) & 0xFFFF;
    }

    executeMULT(immediate, indirect) {
        const value = this.getOperand(immediate, indirect);
        this.registers.ACC = (this.registers.ACC * value) & 0xFFFF;
    }

    executeDIVIDE(immediate, indirect) {
        const value = this.getOperand(immediate, indirect);
        if (value === 0) {
            throw new Error('Divisão por zero');
        }
        this.registers.ACC = Math.floor(this.registers.ACC / value) & 0xFFFF;
    }

    executeLOAD(immediate, indirect) {
        const value = this.getOperand(immediate, indirect);
        this.registers.ACC = value;
    }

    executeSTORE(immediate, indirect) {
        const address = this.getOperandAddress(immediate, indirect);
        if (address >= this.memory.length) {
            throw new Error('Endereço de armazenamento fora dos limites');
        }
        this.memory[address] = this.registers.ACC;
    }

    executeWRITE(immediate, indirect) {
        const value = this.getOperand(immediate, indirect);
        this.outputBuffer.push(value);
        this.updateOutputDisplay();
    }

    executeREAD(immediate, indirect) {
        if (this.inputQueue.length === 0) {
            throw new Error('Fila de entrada vazia');
        }
        const value = this.inputQueue.shift();
        const address = this.getOperandAddress(immediate, indirect);
        if (address >= this.memory.length) {
            throw new Error('Endereço de leitura fora dos limites');
        }
        this.memory[address] = value;
        this.updateInputDisplay();
    }

    executeCOPY(immediate, indirect1, indirect2) {
        const sourceAddr = this.getOperandAddress(false, indirect1);
        const destAddr = this.getOperandAddress(immediate, indirect2);
        
        if (sourceAddr >= this.memory.length || destAddr >= this.memory.length) {
            throw new Error('Endereço de cópia fora dos limites');
        }
        
        this.memory[destAddr] = this.memory[sourceAddr];
    }

    executeCALL(immediate, indirect) {
        this.pushStack(this.registers.PC);
        const address = this.getOperandAddress(immediate, indirect);
        this.registers.PC = address;
    }

    executeRET() {
        this.registers.PC = this.popStack();
    }

    executePUSH() {
        this.pushStack(this.registers.ACC);
    }

    executePOP() {
        this.registers.ACC = this.popStack();
    }

    executeSTOP() {
        this.showMessage('Programa finalizado com STOP', 'success');
        this.stop();
    }

    pushStack(value) {
        if (this.registers.SP >= this.stackLimit) {
            this.registers.PC = 0;
            throw new Error('Stack Overflow');
        }
        this.registers.SP++;
        this.memory[this.registers.SP + 1] = value;
    }

    popStack() {
        if (this.registers.SP <= 0) {
            throw new Error('Stack Underflow');
        }
        const value = this.memory[this.registers.SP + 1];
        this.registers.SP--;
        return value;
    }

    step() {
        if (!this.isRunning) return false;
        const result = this.executeInstruction();
        this.updateDisplay();
        return result;
    }

    run() {
        if (!this.isRunning) return;
        
        const mode = parseInt(document.getElementById('modeSelect').value);
        
        if (mode === 0) {
            while (this.isRunning && this.executeInstruction()) {
                this.cycleCount++;
            }
            this.updateDisplay();
        } else if (mode === 1) {
            const executeStep = () => {
                if (this.isRunning && !this.isPaused) {
                    if (this.executeInstruction()) {
                        this.updateDisplay();
                        setTimeout(executeStep, 100);
                    }
                }
            };
            executeStep();
        } else {
            this.step();
        }
    }

    start() {
        this.isRunning = true;
        this.isPaused = false;
        this.run();
        this.updateStatus();
    }

    stop() {
        this.isRunning = false;
        this.isPaused = false;
        this.updateStatus();
    }

    reset() {
        this.isRunning = false;
        this.isPaused = false;
        this.cycleCount = 0;
        this.currentInstruction = null;
        this.registers = {
            PC: 0, SP: 0, ACC: 0, MOP: parseInt(document.getElementById('modeSelect').value),
            RI: 0, RE: 0, R0: 0, R1: 0
        };
        this.inputQueue = [];
        this.outputBuffer = [];
        this.memory = new Array(1024).fill(0);
        this.updateDisplay();
        this.updateStatus();
        this.showMessage('Máquina resetada', 'success');
    }

    updateDisplay() {
        this.updateMemoryDisplay();
        this.updateRegistersDisplay();
        this.updateCurrentInstructionDisplay();
        this.updateStatus();
    }

    updateMemoryDisplay() {
        const grid = document.getElementById('memoryGrid');
        grid.innerHTML = '';
        
        for (let i = 0; i < 64; i += 8) {
            const row = document.createElement('div');
            row.className = 'memory-row';
            
            const addrCell = document.createElement('div');
            addrCell.className = 'memory-address';
            addrCell.textContent = i.toString(16).toUpperCase().padStart(4, '0');
            row.appendChild(addrCell);
            
            for (let j = 0; j < 8; j++) {
                const cell = document.createElement('div');
                cell.className = 'memory-cell';
                const addr = i + j;
                const value = this.memory[addr] || 0;
                cell.textContent = value.toString(16).toUpperCase().padStart(4, '0');
                
                if (addr === this.registers.PC) {
                    cell.classList.add('current-instruction');
                }
                
                if (addr > 1 && addr <= this.registers.SP + 1) {
                    cell.classList.add('stack');
                }
                
                row.appendChild(cell);
            }
            
            grid.appendChild(row);
        }
    }

    updateRegistersDisplay() {
        const grid = document.getElementById('registersGrid');
        grid.innerHTML = '';
        
        const registerInfo = {
            'PC': { name: 'Program Counter', value: this.registers.PC },
            'SP': { name: 'Stack Pointer', value: this.registers.SP },
            'ACC': { name: 'Acumulador', value: this.registers.ACC },
            'MOP': { name: 'Modo Operação', value: this.registers.MOP },
            'RI': { name: 'Reg. Instrução', value: this.registers.RI },
            'RE': { name: 'Reg. Endereço', value: this.registers.RE },
            'R0': { name: 'Reg. Geral 0', value: this.registers.R0 },
            'R1': { name: 'Reg. Geral 1', value: this.registers.R1 }
        };
        
        Object.entries(registerInfo).forEach(([key, info]) => {
            const regDiv = document.createElement('div');
            regDiv.className = 'register';
            
            const nameDiv = document.createElement('div');
            nameDiv.className = 'register-name';
            nameDiv.textContent = `${key} - ${info.name}`;
            
            const valueDiv = document.createElement('div');
            valueDiv.className = 'register-value';
            valueDiv.innerHTML = `
                Dec: ${info.value}<br>
                Hex: 0x${info.value.toString(16).toUpperCase().padStart(4, '0')}<br>
                Bin: ${info.value.toString(2).padStart(16, '0')}
            `;
            
            regDiv.appendChild(nameDiv);
            regDiv.appendChild(valueDiv);
            grid.appendChild(regDiv);
        });
    }

    updateCurrentInstructionDisplay() {
        const instrDiv = document.getElementById('currentInstruction');
        const detailsDiv = document.getElementById('instructionDetails');
        
        if (this.currentInstruction) {
            instrDiv.style.display = 'block';
            
            let details = `
                <strong>Endereço:</strong> 0x${this.currentInstruction.address.toString(16).toUpperCase().padStart(4, '0')}<br>
                <strong>Instrução:</strong> ${this.currentInstruction.mnemonic} (${this.currentInstruction.opcode})<br>
                <strong>Código:</strong> 0x${this.currentInstruction.instruction.toString(16).toUpperCase().padStart(4, '0')}<br>
                <strong>Binário:</strong> ${this.currentInstruction.instruction.toString(2).padStart(16, '0')}<br>
            `;
            
            if (this.currentInstruction.immediate) details += '<strong>Modo:</strong> Imediato<br>';
            if (this.currentInstruction.indirect1) details += '<strong>Indireto 1:</strong> Sim<br>';
            if (this.currentInstruction.indirect2) details += '<strong>Indireto 2:</strong> Sim<br>';
            
            detailsDiv.innerHTML = details;
        } else {
            instrDiv.style.display = 'none';
        }
    }

    updateStatus() {
        document.getElementById('machineStatus').textContent = 
            this.isRunning ? (this.isPaused ? 'Pausado' : 'Executando') : 'Parado';
        
        const mode = parseInt(document.getElementById('modeSelect').value);
        const modeNames = ['Contínuo', 'Visual', 'Depuração'];
        document.getElementById('currentMode').textContent = modeNames[mode];
        
        document.getElementById('cycleCount').textContent = this.cycleCount;
        document.getElementById('stackInfo').textContent = `${this.registers.SP}/${this.stackLimit}`;
        document.getElementById('overflowStatus').textContent = 
            this.registers.SP >= this.stackLimit ? 'Sim' : 'Não';
    }

    updateOutputDisplay() {
        const outputArea = document.getElementById('outputArea');
        outputArea.innerHTML = this.outputBuffer.map((value, index) => 
            `<div>[${index}] ${value} (0x${value.toString(16).toUpperCase()})</div>`
        ).join('');
        outputArea.scrollTop = outputArea.scrollHeight;
    }

    updateInputDisplay() {
        const inputArea = document.getElementById('inputQueue');
        inputArea.innerHTML = this.inputQueue.map((value, index) => 
            `<div>[${index}] ${value} (0x${value.toString(16).toUpperCase()})</div>`
        ).join('');
    }

    addInput(value) {
        if (value !== undefined && value !== null && !isNaN(value)) {
            this.inputQueue.push(parseInt(value));
            this.updateInputDisplay();
            return true;
        }
        return false;
    }

    showMessage(message, type = 'info') {
        const statusDiv = document.getElementById('statusMessage');
        statusDiv.innerHTML = `<div class="${type}">${message}</div>`;
        setTimeout(() => {
            statusDiv.innerHTML = '';
        }, 5000);
    }
}

let vm = new VirtualMachine();

document.getElementById('fileInput').addEventListener('change', function(e) {
    const file = e.target.files[0];
    if (file) {
        const reader = new FileReader();
        reader.onload = function(e) {
            const content = e.target.result;
            const binaryData = content.replace(/\s+/g, '');
            
            if (vm.loadProgram(binaryData)) {
                document.getElementById('fileName').textContent = file.name;
            }
        };
        reader.readAsText(file);
    }
});

function startExecution() {
    vm.start();
}

function stepExecution() {
    if (!vm.isRunning) {
        vm.isRunning = true;
    }
    vm.step();
}

function stopExecution() {
    vm.stop();
}

function resetMachine() {
    vm.reset();
    document.getElementById('fileName').textContent = 'Nenhum';
}

function addInput() {
    const input = document.getElementById('inputValue');
    const value = parseInt(input.value);
    
    if (!isNaN(value) && value >= -32768 && value <= 65535) {
        vm.addInput(value);
        input.value = '';
        vm.showMessage(`Valor ${value} adicionado à entrada`, 'success');
    } else {
        vm.showMessage('Valor inválido. Use valores entre -32768 e 65535', 'error');
    }
}

document.getElementById('inputValue').addEventListener('keypress', function(e) {
    if (e.key === 'Enter') {
        addInput();
    }
});

document.getElementById('modeSelect').addEventListener('change', function() {
    vm.registers.MOP = parseInt(this.value);
    vm.updateStatus();
});

document.addEventListener('DOMContentLoaded', function() {
    vm.showMessage('Simulador carregado. Carregue um arquivo .txt com código binário para começar.', 'success');
});