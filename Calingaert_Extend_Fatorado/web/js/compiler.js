document.addEventListener('DOMContentLoaded', function() {
    const compileBtn = document.getElementById('compileBtn');
    const downloadBtn = document.getElementById('downloadBtn');
    const machineCodeArea = document.getElementById('machine_code_area');
    const statusEl = document.getElementById('status');

    // Função para gerar o arquivo de download
    function generateDownloadFile(content) {
        const blob = new Blob([content], { type: 'text/plain' });
        const url = URL.createObjectURL(blob);
        downloadBtn.href = url;
        downloadBtn.download = 'programa.txt';
        downloadBtn.style.display = 'inline-block';
    }

    compileBtn.addEventListener('click', async () => {
        statusEl.textContent = 'Compilando...';
        statusEl.className = 'warning';
        machineCodeArea.value = '';
        downloadBtn.style.display = 'none';

        try {
            // Simulação de compilação - substituir por chamada real à API
            const response = await simulateCompilation();
            
            if (response.error) {
                throw new Error(response.error);
            }

            machineCodeArea.value = response.code;
            statusEl.textContent = 'Compilação concluída com sucesso!';
            statusEl.className = 'success';
            
            // Prepara o arquivo para download
            generateDownloadFile(response.code);
        } catch (error) {
            console.error('Falha na compilação:', error);
            statusEl.textContent = `Erro: ${error.message}`;
            statusEl.className = 'error';
        }
    });

    // Função de simulação (substituir por chamada real à API)
    function simulateCompilation() {
        return new Promise((resolve, reject) => {
            setTimeout(() => {
                // Simula um código de máquina de 16 bits
                const code = Array.from({length: 32}, () => 
                    Math.floor(Math.random() * 65536).toString(2).padStart(16, '0')
                ).join('\n');
                
                resolve({ code });
                
                // Para simular erro:
                // reject({ error: "Erro de sintaxe na linha 10" });
            }, 1000);
        });
    }
});