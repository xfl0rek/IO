import { AppBar, Button, Container, Toolbar } from '@mui/material'
import { ReactNode } from 'react'
import { Pathnames } from '../../../router/pathnames'
import { useNavigate } from 'react-router-dom'

interface LayoutProps {
    children: ReactNode
}

export const DefaultLayout = ({ children }: LayoutProps) => {
    // TODO tutaj należy dodawać przeciski do paska nawigacji. Jeśli chcemy, aby był dostępny dla któregoś aktora, należy go umieścić w odpowiednim widoku
    const navigate = useNavigate()


    return (
        <div>
            <AppBar position="static">
                <Toolbar sx={{ display: 'flex'}}>
                    <Button onClick={() => navigate(Pathnames.default.homePage)} sx={{ my: 2, mx: 2, color: 'white' }}>
                        Home
                    </Button>
                    <Button onClick={() => navigate(Pathnames.default.pomPage)} sx={{ my: 2, mx: 2, color: 'white' }}>
                        Pom
                    </Button>
                    {/*//todo do wywalenia*/}
                    <Button onClick={() => navigate(Pathnames.default.externalForm)} sx={{ my: 2, mx: 2, color: 'white' }}>
                        Form
                    </Button>
                    {/*//todo do wywalenia*/}
                </Toolbar>
            </AppBar>
            <Container sx={{ p: 2 }}>
                {children}
            </Container>
        </div>
    )
}
